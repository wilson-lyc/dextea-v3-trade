package cn.dextea.trade.catalog.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Gallery {

    private Long id;

    private String url;

    private String objectKey;

    private LocalDateTime createdAt;

    private String name;
}
