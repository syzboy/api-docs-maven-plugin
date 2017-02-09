package com.appdirect.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class API {
    /**
     * Enunciate file path relative to {@link EnunciateConfig#getConfigFileRootPath()}.
     */
    private String relativeConfigFileName;

    /**
     * API name that will appear as a section header
     */
    private String apiName;
}
