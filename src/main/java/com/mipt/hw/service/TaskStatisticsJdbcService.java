package com.mipt.hw.service;

import com.mipt.hw.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskStatisticsJdbcService {

  private final JdbcTemplate jdbcTemplate;

  public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Map<Priority, Long> getTasksCountByPriority() {
    String sql = "SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority";

    RowMapper<Map.Entry<Priority, Long>> rowMapper = (rs, rowNum) -> {
      Priority priority = Priority.valueOf(rs.getString("priority"));
      Long count = rs.getLong("count");
      return Map.entry(priority, count);
    };

    Map<Priority, Long> statistics = new HashMap<>();
    jdbcTemplate.query(sql, rowMapper)
      .forEach(entry -> statistics.put(entry.getKey(), entry.getValue()));

    return statistics;
  }

  public Map<String, Long> getTasksCountByPriorityAsString() {
    String sql = "SELECT priority, COUNT(*) as count FROM tasks GROUP BY priority";

    RowMapper<PriorityStatDto> rowMapper = (rs, rowNum) ->
      new PriorityStatDto(rs.getString("priority"), rs.getLong("count"));

    Map<String, Long> statistics = new HashMap<>();
    jdbcTemplate.query(sql, rowMapper)
      .forEach(dto -> statistics.put(dto.getPriority(), dto.getCount()));

    return statistics;
  }

  public static class PriorityStatDto {
    private final String priority;
    private final long count;

    public PriorityStatDto(String priority, long count) {
      this.priority = priority;
      this.count = count;
    }

    public String getPriority() { return priority; }
    public long getCount() { return count; }
  }
}
