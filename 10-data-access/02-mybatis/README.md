# Mybatis

![image-20200628113430985](../../assets/image-20200628113430985.png)



## 架构分层

![image-20200218235122850](../../assets/image-20200218235122850.png)

#### 接口层

接口层的意思就是通常是对用户友好的,进行一定细节屏蔽的层

## 核心的类

- Configuration 存放很多核心的注册类和配置信息
  - MapperRegistry 存放Mapper 接口与工厂之间的关系
  - TypeHandlerRegistry 存放 TypeHandler 用来解析 java 类和 JDBC 类型的转换
  - TypeAliasRegistry 存放类型的别名
  - InterceptorChain 存放解析的插件

- SqlSession 接口层核心接口 
  - DefaultSqlSession 中持有了 Executor 用来
    - 对执行SQL 语句
    - 查询缓存的维护等

- StatementHandler 主要是用来处理 Statement 也就是 xml 中文的一个 select .update 等标签 
- ParameterHandler 
- ResultSetHandler

- MapperProxy Mapper 代理对象,Mapper 往往是个接口,没有实现类,底层实际上使用的 JDK 代理动态省了一个 MapperProxy 作为实现类,用于绑定 Statement
- MappedStatement 用于存储一个 Statement 的所有信息

### 四个核心对象:

| 对象                     | 声明周期                    |
| ------------------------ | --------------------------- |
| SqlSessionFactoryBuilder | 方法局部 (method)           |
| SqlSessionFactory(单例)  | 应用级别 (application)      |
| SqlSession               | 请求和操作 (request/method) |
| Mapper                   | 方法 (method)               |

####  [SqlSessionFactoryBuilder](03-sql-session-factory-builder.md) 

**一般来说作用域是方法局部**

这个类可以被实例化、使用和丢弃，一旦创建了 SqlSessionFactory，就不再需要它了。 因此 SqlSessionFactoryBuilder 实例的最佳作用域是方法作用域（也就是局部方法变量）。 你可以重用 SqlSessionFactoryBuilder 来创建多个 SqlSessionFactory 实例，但是最好还是不要让其一直存在，以保证所有的 XML 解析资源可以被释放给更重要的事情。

####  [SqlSessionFactory](02-sql-session-factory.md) 

**一般来说是单例的,作用域是应用级别**

SqlSessionFactory 一旦被创建就应该在应用的运行期间一直存在，没有任何理由丢弃它或重新创建另一个实例。 使用 SqlSessionFactory 的最佳实践是在应用运行期间不要重复创建多次，多次重建 SqlSessionFactory 被视为一种代码“坏味道（bad smell）”。因此 SqlSessionFactory 的最佳作用域是应用作用域。 有很多方法可以做到，最简单的就是使用单例模式或者静态单例模式。

####  [SqlSession](04-sql-session.md) 

**一般来说作用域是请求和操作级别(request/method)**

每个线程都应该有它自己的 SqlSession 实例。SqlSession 的实例不是线程安全的，因此是不能被共享的，所以它的最佳的作用域是请求或方法作用域。 绝对不能将 SqlSession 实例的引用放在一个类的静态域，甚至一个类的实例变量也不行。 也绝不能将 SqlSession 实例的引用放在任何类型的托管作用域中，比如 Servlet 框架中的 HttpSession。 如果你现在正在使用一种 Web 框架，要考虑 SqlSession 放在一个和 HTTP 请求对象相似的作用域中。 换句话说，每次收到的 HTTP 请求，就可以打开一个 SqlSession，返回一个响应，就关闭它。 这个关闭操作是很重要的，你应该把这个关闭操作放到 finally 块中以确保每次都能执行关闭。 下面的示例就是一个确保 SqlSession 关闭的标准模式：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  // 你的应用逻辑代码
}
```

####  [Mapper (映射器)](12-mappers.md) 

一般来说作用域是方法(method)

实际上就是一个代理对象,可以从 SqlSession 中获取,它的作用是用来发送 SQL 来操作数据库的数据,它应该在一个 SqlSession 事务方法之内,实际上在底层创建后,这个接口的实现类就是一个 JDK 代理的` MapperProxy`对象

映射器是一些由你创建的、绑定你映射的语句的接口。映射器接口的实例是从 SqlSession 中获得的。因此从技术层面讲，任何映射器实例的最大作用域是和请求它们的 SqlSession 相同的。尽管如此，映射器实例的最佳作用域是方法作用域。 也就是说，映射器实例应该在调用它们的方法中被请求，用过之后即可丢弃。 并不需要显式地关闭映射器实例，尽管在整个请求作用域保持映射器实例也不会有什么问题，但是你很快会发现，像 SqlSession 一样，在这个作用域上管理太多的资源的话会难于控制。 为了避免这种复杂性，最好把映射器放在方法作用域内。下面的示例就展示了这个实践：

```java
try (SqlSession session = sqlSessionFactory.openSession()) {
  BlogMapper mapper = session.getMapper(BlogMapper.class);
  // 你的应用逻辑代码
}
```
