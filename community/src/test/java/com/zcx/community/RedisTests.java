package com.zcx.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    // 访问string
    public void testStrings() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    // 访问hash
    public void testHashes() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zcx");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    // 访问list
    public void testLists() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);
        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    // 访问无序集合
    public void testSets() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "雪之下","千反田","崛北");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    // 访问有序集合
    public void testSortedSets() {
        String redisKey = "test:students";
        redisTemplate.opsForZSet().add(redisKey, "大老师", 80);
        redisTemplate.opsForZSet().add(redisKey, "折捧", 70);
        redisTemplate.opsForZSet().add(redisKey, "绫小路", 100);
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "大老师"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "大老师"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    @Test
    // 公共api
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    @Test
    // 多次访问同一个key
    public void testBoundOperations() {
        String redisKey = "test:count";
        // 此次演示绑定的是value这个数据类型，也可以是hash或者set之类的
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        // 绑定好了就不用反复传入key了
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    // 演示编程式事务
    public void testTransactional() {
        // 最后会返回一个object，里面有执行操作后受影响的行数，以及数据的样子
        Object object = redisTemplate.execute(new SessionCallback() {
            @Override
            // 利用RedisOperations来管理事务，执行命令
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                // 启动事务
                operations.multi(); 
                // 执行操作
                operations.opsForSet().add(redisKey, "爱丽丝");
                operations.opsForSet().add(redisKey, "唐海音");
                System.out.println(operations.opsForSet().members(redisKey)); // 这时候查询是得不到数据的，因为这些操作都会被放到队列里，在提交事务之后才会被统一传给Redis开始执行。因此在Redis管理事务的中间不要查询数据
                // 提交事务
                return operations.exec();
            }
        });
        System.out.println(object);
    }
}
