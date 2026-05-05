package org.example.multileanproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình cho uploads
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);

        // --- THÊM CÁI NÀY ĐỂ HIỆN ẢNH THUMBNAIL ---
        // Vì SQL của bạn đang lưu đường dẫn là /images/abc.jpg
        registry
                .addResourceHandler("/images/**")
                .addResourceLocations("file:images/", "classpath:/static/images/")
                .setCachePeriod(3600);

        // Certificate PDFs generated on disk
        registry
                .addResourceHandler("/generated-certificates/**")
                .addResourceLocations("file:generated-certificates/")
                .setCachePeriod(3600);
    }
}
