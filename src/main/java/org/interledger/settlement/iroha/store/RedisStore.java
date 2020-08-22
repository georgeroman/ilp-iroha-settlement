package org.interledger.settlement.iroha.store;

import org.interledger.settlement.iroha.config.DefaultArgumentValues;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

@Component
public class RedisStore implements Store {
  private static final String SETTLEMENT_ACCOUNTS = "settlement_accounts";
  private static final String LEFTOVERS = "leftovers";
  private static final String REQUEST_STATUSES = "request_statuses";
  private static final String CHECKED_TXS = "checked_txs";
  private static final String UNCHECKED_TXS = "unchecked_txs";

  @Value("${redis-url:" + DefaultArgumentValues.REDIS_URL + "}")
  private String redisUrl;

  private JedisPool jedisPool;

  /**
   * <p>Initializes the connection to Redis.</p>
   */
  @PostConstruct
  public void init() {
    try {
      URI url = new URI(this.redisUrl);
      this.jedisPool = new JedisPool(new JedisPoolConfig(), url.getHost(), url.getPort());
    } catch (URISyntaxException err) {
      // Should be unreachable, as validation is done at startup
    }
  }

  @Override
  public String getSettlementAccountId(String peerIrohaAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      for (String settlementAccountId : jedis.hkeys(SETTLEMENT_ACCOUNTS)) {
        if (jedis.hget(SETTLEMENT_ACCOUNTS, settlementAccountId).equals(peerIrohaAccountId)) {
          return settlementAccountId;
        }
      }
      return null;
    }
  }

  @Override
  public boolean existsSettlementAccount(String settlementAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.hexists(SETTLEMENT_ACCOUNTS, settlementAccountId);
    }
  }

  @Override
  public void deleteSettlementAccount(String settlementAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.hdel(SETTLEMENT_ACCOUNTS, settlementAccountId);
      jedis.hdel(LEFTOVERS, settlementAccountId);
    }
  }

  @Override
  public void savePeerIrohaAccountId(String settlementAccountId, String peerIrohaAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.hset(SETTLEMENT_ACCOUNTS, settlementAccountId, peerIrohaAccountId);
    }
  }

  @Override
  public String getPeerIrohaAccountId(String settlementAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.hget(SETTLEMENT_ACCOUNTS, settlementAccountId);
    }
  }

  @Override
  public void saveLeftover(String settlementAccountId, BigDecimal leftover) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.hset(LEFTOVERS, settlementAccountId, leftover.toString());
    }
  }

  @Override
  public BigDecimal getLeftover(String settlementAccountId) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String leftover = jedis.hget(LEFTOVERS, settlementAccountId);
      if (leftover == null) {
        return BigDecimal.ZERO;
      } else {
        return new BigDecimal(leftover);
      }
    }
  }

  @Override
  public void saveCheckedTx(String txHash) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.rpush(CHECKED_TXS, txHash);
    }
  }

  @Override
  public void saveUncheckedTx(String txHash) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.rpush(UNCHECKED_TXS, txHash);
    }
  }

  @Override
  public String getLastCheckedTxHash() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      if (jedis.llen(CHECKED_TXS) > 0) {
        return jedis.lindex(CHECKED_TXS, -1);
      } else {
        return null;
      }
    }
  }

  @Override
  public List<String> getUncheckedTxHashes() {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.lrange(UNCHECKED_TXS, 0, -1);
    }
  }

  @Override
  public boolean wasTxChecked(String txHash) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      return jedis.lrange(CHECKED_TXS, 0, -1).contains(txHash);
    }
  }

  @Override
  public void saveRequestStatus(String idempotencyKey, Integer status) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      jedis.hset(REQUEST_STATUSES, idempotencyKey, status.toString());
    }
  }

  @Override
  public Integer getRequestStatus(String idempotencyKey) {
    try (Jedis jedis = this.jedisPool.getResource()) {
      String status = jedis.hget(REQUEST_STATUSES, idempotencyKey);
      if (status == null) {
        return null;
      } else {
        return Integer.parseInt(status);
      }
    }
  }
}
