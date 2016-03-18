package project.demo.redisdemo;

import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.HostAndPort;

public class BinaryJedisClusterTest {

	private static BinaryJedisCluster jc;
	static {
		// 只给集群里一个实例就可以
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 6380));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 6381));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 6382));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 6383));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 6384));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 7380));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 7381));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 7382));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 7383));
		jedisClusterNodes.add(new HostAndPort("10.10.34.14", 7384));
		jc = new BinaryJedisCluster(jedisClusterNodes);
	}

	
}
