package ru.x5.motpsender.dao.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDateTime;

@RedisHash("motpToken")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class MotpToken {
    @Id
    private String inn;
    private String signerId;
    private String token;
    private Integer lifetime;
    private LocalDateTime tokenDateTime;
}
