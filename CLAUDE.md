# Yudao Boot Mini - CLAUDE Assistant Guide

## Project Overview

**Yudao Boot Mini** is a Spring Boot-based rapid development platform, offering a streamlined version of the comprehensive RuoYi-Vue-Pro system. This mini version focuses on core system functionality and infrastructure features, making it ideal for projects requiring a solid foundation without the complexity of full-featured enterprise modules.

**Key Characteristics**:
- **Java 8 + Spring Boot 2.7.18** based
- **Multi-module Maven** architecture
- **Domain-Driven Design** approach
- **Production-proven** architecture

## Technology Stack

### Core Technologies
- **Java**: JDK 8 (LTS)
- **Spring Boot**: 2.7.18
- **Spring Framework**: 5.3.39
- **Spring Security**: 5.7.11
- **MyBatis Plus**: 3.5.7
- **Database**: MySQL 5.7/8.0+ (primary), Oracle, PostgreSQL, DM, Kingbase, OpenGauss, SQL Server, TiDB
- **Cache**: Redis 5.0/6.0/7.0 + Redisson 3.51.0
- **Workflow**: Flowable 6.8.0
- **Build Tool**: Maven 3.6+
- **Containerization**: Docker

### Supporting Technologies
- **API Documentation**: Swagger/OpenAPI 3
- **Message Queue**: RocketMQ, Kafka, Redis Streams
- **Monitoring**: Spring Boot Admin, SkyWalking
- **File Storage**: Local, S3, FTP, SFTP
- **Real-time**: WebSocket
- **Testing**: JUnit 5, Mockito, TestContainers

## Project Structure

### Root Level
```
yudao-boot-mini/
├── yudao-dependencies/     # Maven BOM - Central dependency management
├── yudao-framework/       # Core framework extensions and starters
├── yudao-server/          # Main application entry point (thin container)
├── yudao-module-system/    # System management (users, roles, permissions)
├── yudao-module-infra/     # Infrastructure (files, config, jobs)
├── yudao-module-member/    # Member management
├── sql/                    # Database initialization scripts
├── script/                 # Build and deployment scripts
└── .image/                 # Docker images and resources
```

### Module Architecture
Each business module follows a consistent structure:
```
yudao-module-{domain}/
├── api/                   # APIs provided to other modules
├── controller/            # REST controllers (admin/app)
├── service/               # Business service layer
├── dal/                   # Data Access Layer
│   ├── dataobject/       # Entity classes (JPA annotations)
│   └── mapper/           # MyBatis mappers and XML
├── convert/               # Object converters (MapStruct)
├── enums/                 # Domain enums
├── framework/             # Module-specific framework extensions
├── job/                   # Scheduled tasks (Quartz)
├── mq/                    # Message queue handlers
└── util/                  # Utility classes
```

### Framework Modules
The `yudao-framework` contains reusable Spring Boot starters:
- `yudao-spring-boot-starter-web` - Web layer enhancements
- `yudao-spring-boot-starter-security` - Security extensions
- `yudao-spring-boot-starter-mybatis` - MyBatis enhancements
- `yudao-spring-boot-starter-redis` - Redis integration
- `yudao-spring-boot-starter-protection` - Security features
- `yudao-spring-boot-starter-mq` - Message queue support
- `yudao-spring-boot-starter-job` - Scheduled tasks
- `yudao-spring-boot-starter-excel` - Excel export/import

## Development Commands

### Maven Commands
```bash
# Build the entire project
mvn clean package

# Build with specific profile
mvn clean package -P development

# Build with profiles for different environments
mvn clean package -P development
mvn clean package -P test
mvn clean package -P production

# Run tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Skip tests during build
mvn clean package -DskipTests

# Install to local repository
mvn clean install

# Check dependencies for updates
mvn versions:display-dependency-updates
```

### Docker Commands
```bash
# Start the complete development stack
docker-compose up -d

# View logs
docker-compose logs -f

# Stop and cleanup
docker-compose down

# Rebuild and restart
docker-compose up -d --build

# Access specific services
docker-compose exec mysql mysql -u root -p
docker-compose exec redis redis-cli
```

### Development Scripts
```bash
# Deploy script (production-like deployment)
./script/shell/deploy.sh

# The deploy script includes:
# - Backup of existing application
# - Graceful shutdown
# - New deployment
# - Health checks
# - Log monitoring
```

## Architecture Patterns

### 1. Multi-Module Maven Architecture
- **Parent POM**: Centralized dependency management and build configuration
- **Framework Modules**: Reusable components with clear separation of concerns
- **Business Modules**: Domain-specific functionality with bounded contexts
- **Application Module**: Thin aggregation layer for deployment

### 2. Layered Architecture
```
┌─────────────────┐
│   Controller    │  REST API endpoints, request validation
├─────────────────┤
│     Service     │  Business logic, transaction management
├─────────────────┤
│      DAL        │  Data access, repository pattern
├─────────────────┤
│   Database      │  Persistent storage
└─────────────────┘
```

### 3. Domain-Driven Design
- **Bounded Contexts**: Each module represents a clear business domain
- **Domain Services**: Business logic encapsulation
- **Aggregates**: Consistency boundaries within domains
- **Events**: Domain event publishing and handling


## Key Development Patterns

### 1. Controller Pattern
```java
@RestController
@RequestMapping("/admin-api/system/user")
@Tag(name = "管理后台 - 用户")
@Validated
public class UserController {
    
    @Resource
    private UserService userService;
    
    @PostMapping("/create")
    @Operation(summary = "创建用户")
    @PreAuthorize("@ss.hasPermission('system:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
        return success(userService.createUser(createReqVO));
    }
}
```

### 2. Service Layer Pattern
```java
@Service("userService")
@Validated
public class UserServiceImpl implements UserService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserRoleService userRoleService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateReqVO createReqVO) {
        // Validate user doesn't exist
        validateUserForCreate(createReqVO.getUsername());
        
        // Create user
        UserDO user = convertToUserDO(createReqVO);
        userMapper.insert(user);
        
        // Create user roles
        if (CollUtil.isNotEmpty(createReqVO.getRoleIds())) {
            userRoleService.createUserRole(user.getId(), createReqVO.getRoleIds());
        }
        
        // Publish user created event
        userServiceEventPublisher.sendUserCreateEvent(user.getId());
        
        return user.getId();
    }
}
```

### 3. Data Access Pattern
```java
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {
    
    default PageResult<UserDO> selectPage(UserPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserDO>()
                .likeIfPresent(UserDO::getUsername, reqVO.getUsername())
                .eqIfPresent(UserDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(UserDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(UserDO::getId));
    }
}
```

### 4. Conversion Pattern
```java
@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);
    
    UserDO convert(UserCreateReqVO bean);
    
    UserRespVO convert(UserDO bean);
    
    List<UserRespVO> convert(List<UserDO> list);
}
```

## Configuration Guide

### Application Configuration
Main configuration file: `yudao-server/src/main/resources/application.yaml`

Key configuration sections:
```yaml
# Application basics
spring:
  application:
    name: lawsaas-server
  profiles:
    active: local
    
# Database configuration
spring:
  datasource:
    dynamic:
      datasource:
        master:
          url: jdbc:mysql://localhost:3306/ruoyi-vue-pro?...
          username: root
          password: 123456
          
# Redis configuration
spring:
  data:
    redis:
      host: localhost
      port: 6379
      
# Yudao configuration
yudao:
  info:
    base-package: cn.iocoder.yudao
  tenant:
    enable: true
```

### Environment-specific Profiles
- `local`: Local development
- `development`: Development environment
- `test`: Testing environment  
- `production`: Production environment

## Database Setup

### Supported Databases
- **MySQL**: Primary database with full feature support
- **PostgreSQL**: Complete feature compatibility
- **Oracle**: Enterprise database support
- **SQL Server**: Microsoft SQL Server support
- **DM**:国产达梦数据库
- **Kingbase**: 国产人大金仓数据库
- **OpenGauss**: 华为开源数据库
- **TiDB**: 分布式 NewSQL database

### Initialization Scripts
Located in `sql/` directory:
```
sql/
├── mysql/
│   ├── ruoyi-vue-pro.sql      # Main database schema
│   └── quartz.sql             # Quartz scheduler tables
├── postgresql/
├── oracle/
├── sqlserver/
└── ... (other databases)
```

### Database Setup Commands
```bash
# MySQL setup
mysql -u root -p < sql/mysql/ruoyi-vue-pro.sql

# PostgreSQL setup
psql -U postgres < sql/postgresql/ruoyi-vue-pro.sql

# Setup with Docker
docker-compose exec mysql mysql -u root -p123456 < sql/mysql/ruoyi-vue-pro.sql
```

## Security Architecture

### Authentication Flow
1. **Login**: Username/password authentication
2. **Token Generation**: JWT token with user claims
3. **Token Validation**: JWT token validation on each request
4. **Authorization**: Role and permission-based access control

### Key Security Features
- **JWT Authentication**: Stateless authentication
- **RBAC**: Role-Based Access Control
- **Data Permissions**: Fine-grained data access control
- **API Encryption**: Request/response encryption
- **XSS Protection**: Cross-site scripting prevention
- **SQL Injection Protection**: Parameterized queries
- **Rate Limiting**: API endpoint throttling

### Security Configuration
```yaml
yudao:
  security:
    permit-all-urls:
      - /admin-api/auth/login
      - /admin-api/auth/register
      - /actuator/**
  api-encrypt:
    enable: true
    algorithm: AES
```

## Testing Strategy

### Unit Testing
```java
@SpringBootTest
class UserServiceTest {
    
    @Resource
    private UserService userService;
    
    @Test
    void testCreateUser() {
        UserCreateReqVO reqVO = new UserCreateReqVO();
        reqVO.setUsername("test");
        reqVO.setPassword("123456");
        reqVO.setEmail("test@example.com");
        
        Long userId = userService.createUser(reqVO);
        
        assertThat(userId).isNotNull();
    }
}
```

### Integration Testing
- **TestContainers**: Docker-based integration testing
- **@SpringBootTest**: Full context integration testing
- **@WebMvcTest**: Controller layer testing
- **@DataJpaTest**: Repository layer testing

## Code Generation

### Code Generator Features
- **Automatic CRUD Operations**: Generate complete REST APIs
- **Database Reverse Engineering**: Generate entities from existing tables
- **Frontend Code Generation**: Vue.js component generation
- **Multi-table Support**: One-to-many, many-to-many relationships
- **Custom Templates**: Support for custom code templates

### Using Code Generator
1. Access the admin panel at `/admin-api/system/codegen`
2. Configure table selection and generation options
3. Generate and download the code package
4. Integrate generated code into the project

## Monitoring and Observability

### Application Monitoring
- **Spring Boot Admin**: Application monitoring dashboard
- **Health Checks**: Endpoint `/actuator/health`
- **Metrics**: Micrometer integration for metrics collection
- **Logging**: Structured logging with Logback

### Distributed Tracing
- **SkyWalking**: Distributed tracing system
- **OpenTracing**: Tracing API standard
- **Trace Context**: Request correlation across services

### Performance Monitoring
- **Database Monitoring**: SQL performance analysis
- **Cache Monitoring**: Redis hit/miss ratios
- **JVM Monitoring**: Memory, GC, thread pool metrics


## Message Queue Integration

### Supported Brokers
- **RocketMQ**: High-performance distributed messaging
- **Kafka**: Event streaming platform
- **Redis Streams**: Lightweight Redis-based messaging

### Message Processing Pattern
```java
@Service
@RequiredArgsConstructor
public class OrderMessageConsumer {
    
    private final OrderService orderService;
    
    @RocketMQMessageListener(topic = "ORDER_TOPIC", consumerGroup = "order-consumer")
    public void processOrderMessage(String message) {
        OrderMessage orderMessage = JSON.parseObject(message, OrderMessage.class);
        orderService.processOrder(orderMessage);
    }
}
```

## WebSocket Support

### Real-time Features
- **Server Push**: Real-time notifications to clients
- **Chat Systems**: WebSocket-based messaging
- **Live Updates**: Real-time data synchronization
- **Broadcasting**: One-to-many message delivery

### WebSocket Configuration
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DemoWebSocketMessageListener(), "/infra/ws")
                .setAllowedOrigins("*");
    }
}
```

## File Management

### Storage Backends
- **Local File System**: Simple local storage
- **S3 Compatible**: Amazon S3, MinIO, Alibaba OSS
- **FTP/SFTP**: File transfer protocol support
- **Database**: Binary storage in database

### File Upload Pattern
```java
@PostMapping("/upload")
@Operation(summary = "上传文件")
@PreAuthorize("@ss.hasPermission('infra:file:upload')")
public CommonResult<String> uploadFile(@RequestParam("file") MultipartFile file) {
    String fileUrl = fileService.uploadFile(file, "demo");
    return success(fileUrl);
}
```

## API Documentation

### Swagger/OpenAPI Integration
- **Automatic Generation**: API docs from code annotations
- **Interactive Testing**: API testing via Swagger UI
- **Multi-format**: JSON and YAML format support
- **Version Control**: API version management

### API Documentation Access
- **Swagger UI**: `/swagger-ui.html`
- **OpenAPI JSON**: `/v3/api-docs`
- **Knife4j**: Enhanced Swagger UI at `/doc.html`

## Common Development Tasks

### Adding a New Module
1. Create new module directory: `yudao-module-{domain}`
2. Add to parent POM:
```xml
<module>yudao-module-{domain}</module>
```
3. Create module structure following conventions
4. Add dependency to `yudao-server` POM
5. Implement domain logic following established patterns

### Customizing Error Handling
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public CommonResult<?> handleBusinessException(BusinessException ex) {
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }
}
```

### Creating Custom Converters
```java
@Mapper
public class CustomConvert {
    
    @Mapping(target = "targetField", expression = "java(source.getSourceField() + \"_suffix\")")
    TargetDTO convert(SourceDTO source);
}
```

## Performance Optimization

### Caching Strategy
- **L1 Cache**: Local cache for frequently accessed data
- **L2 Cache**: Redis cluster for distributed caching
- **Query Optimization**: MyBatis Plus query optimization
- **Connection Pooling**: HikariCP for database connections

### Database Optimization
- **Index Strategy**: Proper indexing for query patterns
- **Connection Pooling**: Optimized connection management
- **Query Analysis**: Slow query monitoring
- **Partitioning**: Table partitioning for large datasets

### JVM Tuning
```bash
# JVM options for production
-Xms2g -Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/yudao/heapdump.hprof
```

## Deployment Guide

### Production Deployment
1. **Build**: `mvn clean package -P production`
2. **Database Setup**: Initialize production database
3. **Configuration**: Update application configuration
4. **Deployment**: Use deployment script or orchestration
5. **Monitoring**: Set up monitoring and alerts

### Environment Variables
```bash
# Database configuration
SPRING_DATASOURCE_DYNAMIC_DATASOURCE_MASTER_URL=jdbc:mysql://prod-db:3306/ruoyi-vue-pro
SPRING_DATASOURCE_DYNAMIC_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_DYNAMIC_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Redis configuration
SPRING_REDIS_HOST=${REDIS_HOST}
SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}

# Application configuration
SPRING_PROFILES_ACTIVE=production
JAVA_OPTS=-Xms2g -Xmx2g
```

## Troubleshooting

### Common Issues
1. **Database Connection**: Verify database URL and credentials
2. **Port Conflicts**: Ensure ports 48080, 3306, 6379 are available
3. **Memory Issues**: Adjust JVM heap settings

### Debug Mode
```yaml
# Enable debug logging
logging:
  level:
    cn.iocoder.yudao: DEBUG
    org.springframework.web: DEBUG

# Disable production optimizations
spring:
  devtools:
    restart:
      enabled: true
```

## Best Practices

### Code Quality
- **Follow naming conventions**: Clear, descriptive names
- **Keep methods small**: Single responsibility principle
- **Write comprehensive tests**: Unit and integration tests
- **Use design patterns**: Appropriate use of GoF patterns
- **Document thoroughly**: Javadoc for public APIs

### Security Practices
- **Validate all inputs**: Server-side validation
- **Use parameterized queries**: Prevent SQL injection
- **Implement proper authentication**: JWT with proper claims
- **Rate limiting**: Protect against DDoS attacks
- **Regular security updates**: Keep dependencies updated

### Performance Practices
- **Cache strategically**: Cache frequently accessed data
- **Optimize queries**: Use proper indexing
- **Lazy loading**: Load data only when needed
- **Connection pooling**: Reuse database connections
- **Monitor performance**: Track key metrics

This CLAUDE.md file provides a comprehensive guide for working with the Yudao Boot Mini project. Follow these guidelines to ensure consistent, high-quality development that aligns with the project's architecture and standards.
