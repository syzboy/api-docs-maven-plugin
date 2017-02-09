package com.appdirect.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @ToString
public class EnunciateConfig {
    /**
     * Directory where the enunciate files will be generated
     */
    private String docsDir;

    /**
     * Enunciate configuration file location root path
     */
    private String configFileRootPath;
}
