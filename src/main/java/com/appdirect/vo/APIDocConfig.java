package com.appdirect.vo;

import java.io.File;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.google.inject.internal.util.Lists;

@Getter @Setter @NoArgsConstructor @ToString
public class APIDocConfig {
    private EnunciateConfig enunciate;
    private List<API> apis;

    public List<String> getConfigFiles() {
        List<String> configFiles = Lists.newArrayList();
        for(API a : apis) {
            File file = new File(enunciate.getConfigFileRootPath(), a.getRelativeConfigFileName());
            configFiles.add(file.toString());
        }
        return configFiles;
    }
}
