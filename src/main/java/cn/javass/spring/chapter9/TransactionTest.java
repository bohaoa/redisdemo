package cn.javass.spring.chapter9;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import cn.javass.spring.chapter9.model.AddressModel;
import cn.javass.spring.chapter9.model.UserModel;
import cn.javass.spring.chapter9.service.IAddressService;
import cn.javass.spring.chapter9.service.IUserService;


public class TransactionTest {
   
    private static ApplicationContext ctx;
    private static PlatformTransactionManager txManager;
    private static DataSource dataSource;
    private static JdbcTemplate jdbcTemplate;
    
    
    //id自增主键从0开始
    private static final String CREATE_TABLE_SQL = "create table test" +
    "(id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
    "name varchar(100))";
    private static final String DROP_TABLE_SQL = "drop table test";

    
    private static final String CREATE_USER_TABLE_SQL = "create table user" +
    "(id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
    "name varchar(100))";

    private static final String DROP_USER_TABLE_SQL = "drop table user";
    
    private static final String CREATE_ADDRESS_TABLE_SQL = "create table address" +
    "(id int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
    "province varchar(100), city varchar(100), street varchar(100), user_id int)";
    
    private static final String DROP_ADDRESS_TABLE_SQL = "drop table address";

    
    private static final String INSERT_SQL = "insert into test(name) values(?)";
    private static final String COUNT_SQL = "select count(*) from test";
    
    @BeforeClass
    public static void setUpClass() {
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/applicationContext-jdbc.xml"};
        ctx = new ClassPathXmlApplicationContext(configLocations);
        txManager = ctx.getBean(PlatformTransactionManager.class);
        dataSource = ctx.getBean(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    @Test
    public void testPlatformTransactionManagerForLowLevel1() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.prepareStatement(CREATE_TABLE_SQL).execute();
            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            pstmt.setString(1, "test");
            pstmt.execute();
            conn.prepareStatement(DROP_TABLE_SQL).execute();
            txManager.commit(status);
        } catch (Exception e) {
            status.setRollbackOnly();
            txManager.rollback(status);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
    
    @Test
    public void testPlatformTransactionManagerForLowLevel2() {
        
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/applicationContext-jdbc2.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        DataSource dataSourceProxy = ctx2.getBean("dataSourceProxy", DataSource.class);
        
        Connection conn = null;
        try {
            conn = dataSourceProxy.getConnection();
            conn.prepareStatement(CREATE_TABLE_SQL).execute();
            PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL);
            pstmt.setString(1, "test");
            pstmt.execute();
            conn.prepareStatement(DROP_TABLE_SQL).execute();
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
            }
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
        
        
    }
    
    
    
    
    @Test
    public void testPlatformTransactionManagerForHighLevel() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        jdbcTemplate.execute(CREATE_TABLE_SQL);
        try {
            jdbcTemplate.update(INSERT_SQL, "test");
            txManager.commit(status);
        } catch (RuntimeException e) {
            txManager.rollback(status);
        } 
        jdbcTemplate.execute(DROP_TABLE_SQL);
    }
    

    
    
    @Test
    public void testTransactionTemplate() {
        jdbcTemplate.execute(CREATE_TABLE_SQL);
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update(INSERT_SQL, "test");
               
            }
        });
        jdbcTemplate.execute(DROP_TABLE_SQL);
    }

    @Test
    public void testJtaTransactionTemplate() {
        
        String[] configLocations = new String[] {
                "classpath:chapter9/applicationContext-jta-derby.xml"};
        ctx = new ClassPathXmlApplicationContext(configLocations);
        final PlatformTransactionManager jtaTXManager = ctx.getBean(PlatformTransactionManager.class);
        final DataSource derbyDataSource1 = ctx.getBean("dataSource1", DataSource.class);
        final DataSource derbyDataSource2 = ctx.getBean("dataSource2", DataSource.class);
        final JdbcTemplate jdbcTemplate1 = new JdbcTemplate(derbyDataSource1);
        final JdbcTemplate jdbcTemplate2 = new JdbcTemplate(derbyDataSource2);
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(jtaTXManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        jdbcTemplate1.update(CREATE_TABLE_SQL);
        int originalCount = jdbcTemplate1.queryForObject(COUNT_SQL, Integer.class);
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    jdbcTemplate1.update(INSERT_SQL, "test");
                    //因为数据库2没有创建数据库表因此会回滚事务
                    jdbcTemplate2.update(INSERT_SQL, "test");
                }
            });
        } catch (RuntimeException e) {
            int count = jdbcTemplate1.queryForObject(COUNT_SQL, Integer.class);
            Assert.assertEquals(originalCount, count);
        }
        jdbcTemplate1.update(DROP_TABLE_SQL);
    }
    
    @Test
    public void testServiceTransaction() {
        String[] configLocations = new String[] {
        "classpath:chapter7/applicationContext-resources.xml",
        "classpath:chapter9/dao/applicationContext-jdbc.xml",
        "classpath:chapter9/service/applicationContext-service.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
        jdbcTemplate2.update(CREATE_USER_TABLE_SQL);
        jdbcTemplate2.update(CREATE_ADDRESS_TABLE_SQL);
        
        IUserService userService = ctx2.getBean("userService", IUserService.class);
        IAddressService addressService = ctx2.getBean("addressService", IAddressService.class);
        UserModel user = createDefaultUserModel();
        
        userService.save(user);
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
        
        jdbcTemplate2.update(DROP_USER_TABLE_SQL);
        jdbcTemplate2.update(DROP_ADDRESS_TABLE_SQL);
    }
    
    //----------------------------------------------
    //传播行为测试
    //----------------------------------------------
    @Test
    public void testPropagation() {
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/dao/applicationContext-jdbc.xml",
        "classpath:chapter9/service/applicationContext-service.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
       
        
       
        UserModel user = createDefaultUserModel();
        
        //1、Required传播行为
        testRequired(ctx2, jdbcTemplate2, user);

        //2、RequiresNew传播行为
        testRequiresNew(ctx2, jdbcTemplate2, user);
        
        //3、Supports传播行为
        testSupports(ctx2, jdbcTemplate2, user);
        
        //4、NotSupported传播行为
        testNotSupported(ctx2, jdbcTemplate2, user);
        
        //5、Mandatory传播行为
        testMandatory(ctx2, jdbcTemplate2, user);

        //6、Never传播行为
        testNever(ctx2, jdbcTemplate2, user);
        
        //7、Nested传播行为
        testNested(ctx2, jdbcTemplate2, user);
    }
    
    //------------------------------------------
    //事务只读测试
    //------------------------------------------
    @Test
    public void testReadonlyTransaction() {
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/dao/applicationContext-jdbc.xml",
        "classpath:chapter9/service/applicationContext-service.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
        prepareTable(jdbcTemplate2);

        IUserService userService = ctx2.getBean("readonlyUserService", IUserService.class);
        
        ctx2.getBean(DataSourceTransactionManager.class).setValidateExistingTransaction(true);
        try {
            userService.countAll();
        } catch (RuntimeException e) {
            Assert.assertTrue(e instanceof IllegalTransactionStateException);
        }
        cleanTable(jdbcTemplate2);
    }
    
    //----------------------------------
    //配置实现事务控制测试
    //----------------------------------
    @Test
    public void testConfigTransaction() {
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/dao/applicationContext-jdbc.xml",
                "classpath:chapter9/service/applicationContext-service.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
            
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
        jdbcTemplate2.update(CREATE_USER_TABLE_SQL);
        jdbcTemplate2.update(CREATE_ADDRESS_TABLE_SQL);
            
        IUserService userService = ctx2.getBean("proxyUserService", IUserService.class);
        IAddressService addressService = ctx2.getBean("proxyAddressService", IAddressService.class);
        UserModel user = createDefaultUserModel();
        
        userService.save(user);
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
            
        jdbcTemplate2.update(DROP_USER_TABLE_SQL);
        jdbcTemplate2.update(DROP_ADDRESS_TABLE_SQL);
    }
    
    
    //----------------------------------
    //声明式实现事务控制测试
    //----------------------------------
    @Test
    public void testDeclareTransaction() {
        String[] configLocations = new String[] {
        "classpath:chapter7/applicationContext-resources.xml",
        "classpath:chapter9/dao/applicationContext-jdbc.xml",
        "classpath:chapter9/service/applicationContext-service-declare.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
        jdbcTemplate2.update(CREATE_USER_TABLE_SQL);
        jdbcTemplate2.update(CREATE_ADDRESS_TABLE_SQL);
        IUserService userService = ctx2.getBean("userService", IUserService.class);
        IAddressService addressService = ctx2.getBean("addressService", IAddressService.class);
        UserModel user = createDefaultUserModel();
        userService.save(user);
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
        jdbcTemplate2.update(DROP_USER_TABLE_SQL);
        jdbcTemplate2.update(DROP_ADDRESS_TABLE_SQL);
    }

    //----------------------------------
    //声明注解方式实现事务控制测试
    //----------------------------------
    @Test
    public void testAnntationTransaction() {
        String[] configLocations = new String[] {
                "classpath:chapter7/applicationContext-resources.xml",
                "classpath:chapter9/dao/applicationContext-jdbc.xml",
                "classpath:chapter9/service/applicationContext-service-annotation.xml"};
        ApplicationContext ctx2 = new ClassPathXmlApplicationContext(configLocations);
        DataSource dataSource2 = ctx2.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate2 = new JdbcTemplate(dataSource2);
        jdbcTemplate2.update(CREATE_USER_TABLE_SQL);
        jdbcTemplate2.update(CREATE_ADDRESS_TABLE_SQL);
        IUserService userService = ctx2.getBean("userService", IUserService.class);
        IAddressService addressService = ctx2.getBean("addressService", IAddressService.class);
        UserModel user = createDefaultUserModel();
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
        
        jdbcTemplate2.update(DROP_USER_TABLE_SQL);
        jdbcTemplate2.update(DROP_ADDRESS_TABLE_SQL);
    }


    
    private void testRequired(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //Required传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        requiredWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);

        //Required传播行为，并抛出异常，将回滚事务
        prepareTable(jdbcTemplate2);
        requiredWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);        
    }

    private void testRequiresNew(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //RequiresNew传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        requiresNewWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);

        //RequiresNew传播行为，并抛出异常，将回滚部分事务
        prepareTable(jdbcTemplate2);
        requiresNewWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);   
    }

    private void testSupports(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //Required+Supports传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        requiredAndSupportsWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Required+Supports传播行为，并抛出异常，将回滚事务
        prepareTable(jdbcTemplate2);
        requiredAndSupportsWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);

        //Supports+Supports传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        supportsAndSupportsWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Supports+Supports传播行为，并抛出异常，回滚事务对已执行的操作无影响
        prepareTable(jdbcTemplate2);
        supportsAndSupportsWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
    }
    private void testNotSupported(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //Required+Supports传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        requiredAndNotSupportedWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Required+Supports传播行为，并抛出异常，将回滚部分事务
        prepareTable(jdbcTemplate2);
        requiredAndNotSupportedWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Supports+Supports传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        supportsAndNotSupportedWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Supports+Supports传播行为，并抛出异常
        prepareTable(jdbcTemplate2);
        supportsAndNotSupportedWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);

    }
    private void testMandatory(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //Required+Mandatory传播行为，并成功执行
        prepareTable(jdbcTemplate2);
        requiredAndMandatoryWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Required+Mandatory传播行为，并抛出异常，将回滚事务
        prepareTable(jdbcTemplate2);
        requiredAndMandatoryWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        
        //Supports+Mandatory传播行为，并抛出异常
        prepareTable(jdbcTemplate2);
        supportsAndMandatoryWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);


    }
    private void testNever(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
      //Required+Never传播行为，并抛出异常，将回滚部分事务
        prepareTable(jdbcTemplate2);
        requiredAndNeverWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        
        //Supports+Never传播行为，并抛出异常
        prepareTable(jdbcTemplate2);
        supportsAndNeverWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Supports+Never传播行为，并抛出异常,回滚事务对已执行的操作无影响
        prepareTable(jdbcTemplate2);
        supportsAndNeverWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);

    }

    private void testNested(ApplicationContext ctx2, JdbcTemplate jdbcTemplate2, UserModel user) {
        //Required+Nested传播行为，成功执行
        prepareTable(jdbcTemplate2);
        requiredAndNestedWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
      
        //Required+Nested传播行为，并抛出异常，将回滚部分事务
        prepareTable(jdbcTemplate2);
        requiredAndNestedWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Nested+Nested传播行为，并抛出异常
        prepareTable(jdbcTemplate2);
        nestedAndNestedWithSuccess(ctx2, user);
        cleanTable(jdbcTemplate2);
        
        //Nested+Nested传播行为，并抛出异常,回滚事务对已执行的操作无影响
        prepareTable(jdbcTemplate2);
        nestedAndNestedWithRuntimeException(ctx2, user);
        cleanTable(jdbcTemplate2);
    }

    private void requiredWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }

    private void requiredWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAddressServiceWithRuntimeException", IAddressService.class);
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //因为发生回滚，所以countAll都是0
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }
    

    private void requiresNewWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiresNewUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiresNewAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
      
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }

    private void requiresNewWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiresNewUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiresNewAddressServiceWithRuntimeException", IAddressService.class);
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //因为userService中抛出异常，而addressService成功执行，所以有如下结果
        Assert.assertEquals(0, userService.countAll());//事务失败了
        Assert.assertEquals(1, addressService.countAll());//事务成功执行了
        
    }
    
    
    
    private void requiredAndSupportsWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndSupportsUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndSupportsAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
      
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }

    private void requiredAndSupportsWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndSupportsUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndSupportsAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
      
        //因为发生回滚，所以countAll都是0
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }

    private void supportsAndSupportsWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndSupportsUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndSupportsAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    
    private void supportsAndSupportsWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndSupportsUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndSupportsAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //即使发生回滚，由于运行在非事务环境，所以countAll都是1，类似于JDBC无事务环境
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }
    

    
    private void requiredAndNotSupportedWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndNotSupportedUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndNotSupportedAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    
    private void requiredAndNotSupportedWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndNotSupportedUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndNotSupportedAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //由于addressService.save方法允许在非事务环境，因此addressService.countAll()为1
        //而userService的save方法允许在事务环境，且遭遇addressService.save方法抛出的异常，
        //因此需要回滚，所以userService.countAll()为0
        
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }
    
    private void supportsAndNotSupportedWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndNotSupportedUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndNotSupportedAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    
    private void supportsAndNotSupportedWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndNotSupportedUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndNotSupportedAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //即使发生回滚，由于运行在非事务环境，所以countAll都是1，类似于JDBC无事务环境
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }

    
    
    
    private void requiredAndMandatoryWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndMandatoryUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndMandatoryAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    
    private void requiredAndMandatoryWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndMandatoryUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndMandatoryAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //回滚事务，由于userService.save()和addressService.save()运行在同一事务，所以userService.countAll()为0
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }
    
    private void supportsAndMandatoryWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndMandatoryUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndMandatoryAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertTrue(e instanceof IllegalTransactionStateException);
        }
        
        //由于userService.save()方法运行在Supports传播行为中，即无事务支持，因此userService.countAll()将返回1
        //由于addressService必须允许在事务环境，但当前没有事务所以将抛出异常，因此addressService.countAll()返回0
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }

    
    
    
    private void requiredAndNeverWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndNeverUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndNeverAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertTrue(e instanceof IllegalTransactionStateException);
        }
        
        //回滚事务
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }
    private void supportsAndNeverWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndNeverUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndNeverAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }
    private void supportsAndNeverWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("supportsAndNeverUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("supportsAndNeverAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //由于userService.save()方法运行在Supports传播行为中，即无事务支持，即使抛出异常，也不影响前面的操作，因此userService.countAll()将返回1
        //由于addressService.save()方法运行在Never传播行为中,即不支持事务，即使抛出异常，也不影响前面的操作，因此addressService.countAll()返回1
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
    }

    
    
    
    private void requiredAndNestedWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndNestedUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndNestedAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);

        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    private void requiredAndNestedWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("requiredAndNestedUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("requiredAndNestedAddressServiceWithRuntimeException", IAddressService.class);
        
        userService.save(user);
        
        //由于addressService.save()方法事务内抛出异常，因此将回滚该嵌套事务内的操作
        //userService.save()方法捕获了addressService.save()抛出的异常，
        //虽然嵌套事务回滚了，但由于捕获了异常，因此对嵌套事务对外部事务无影响
        //类似于RequiresNew，但嵌套事务属于同一个物理事务，采用JDBC保存点实现回滚到指定保存点
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }
    private void nestedAndNestedWithSuccess(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("nestedAndNestedUserServiceWithSuccess", IUserService.class);
        IAddressService addressService = ctx2.getBean("nestedAndNestedAddressServiceWithSuccess", IAddressService.class);
        
        userService.save(user);
        
        //正确执行，所以countAll都是1 
        Assert.assertEquals(1, userService.countAll());
        Assert.assertEquals(1, addressService.countAll());
        
    }
    private void nestedAndNestedWithRuntimeException(ApplicationContext ctx2, UserModel user) {
        IUserService userService = ctx2.getBean("nestedAndNestedUserServiceWithRuntimeException", IUserService.class);
        IAddressService addressService = ctx2.getBean("nestedAndNestedAddressServiceWithRuntimeException", IAddressService.class);
        
        try {
            userService.save(user);
            Assert.fail();
        } catch (RuntimeException e) {
        }
        
        //外部事务回滚将导致内部事务也回滚
        Assert.assertEquals(0, userService.countAll());
        Assert.assertEquals(0, addressService.countAll());
    }
    
    
    private void prepareTable(JdbcTemplate jdbcTemplate2) {
        jdbcTemplate2.update(CREATE_USER_TABLE_SQL);
        jdbcTemplate2.update(CREATE_ADDRESS_TABLE_SQL);
    }
    
    private void cleanTable(JdbcTemplate jdbcTemplate2) {
        jdbcTemplate2.update(DROP_USER_TABLE_SQL);
        jdbcTemplate2.update(DROP_ADDRESS_TABLE_SQL);        
    }
    
    private UserModel createDefaultUserModel() {
        UserModel user = new UserModel();
        user.setName("test");
        AddressModel address = new AddressModel();
        address.setProvince("beijing");
        address.setCity("beijing");
        address.setStreet("haidian");
        user.setAddress(address);
        return user;
    }

    
}
