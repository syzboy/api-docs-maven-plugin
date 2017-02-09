package com.appdirect;

import java.io.InputStreamReader;

import org.testng.annotations.Test;

import com.appdirect.vo.APIDocConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class APIDocConfigTest {
    @Test
    public void testParseConfig() throws Exception {
        InputStreamReader configFile = new InputStreamReader(getClass().getResourceAsStream("/config.json"));
        ObjectMapper mapper = new ObjectMapper();
        APIDocConfig config = mapper.readValue(configFile, APIDocConfig.class);
    }

//    @Test
//    public void ttt() throws Exception {
//        File f = new File("/Users/pat.vongphrachanh/AppDirect/api-docs-plugin/appdirect-api-docs-plugin/pom.xml");
//        assertThat(f.getName()).isEqualTo("pom.xml");
//    }
}
