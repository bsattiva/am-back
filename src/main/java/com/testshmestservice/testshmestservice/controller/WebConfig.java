    package com.testshmestservice.testshmestservice.controller;

    import com.utils.TestHelper;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.core.io.FileSystemResource;
    import org.springframework.core.io.Resource;
    import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
    import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
    import org.springframework.web.servlet.resource.PathResourceResolver;

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            var path = TestHelper.getSameLevelProject("files");
            registry.addResourceHandler("/files/**")
                    .addResourceLocations("file:/" + path + "/")
                   // .addResourceLocations("file:/C:/Users/Maxim.Karpov/IdeaProjects/files/")
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver() {
                        @Override
                        protected Resource getResource(String resourcePath, Resource location) {
                            String id = extractIdFromResourcePath(resourcePath);
                            if (id == null) {
                                // Handle when id is not present or invalid
                                return null;
                            }

                            String filePath = "output_" + id + ".pdf";
                            return new FileSystemResource(path + "/" + filePath);
                        }
                    });
        }

        private String extractIdFromResourcePath(String resourcePath) {
            int startIndex = resourcePath.lastIndexOf("output_") + "output_".length();
            int endIndex = resourcePath.lastIndexOf(".pdf");
            if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
                return resourcePath.substring(startIndex, endIndex);
            }
            return null;
        }

    }