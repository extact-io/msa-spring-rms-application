package io.extact.msa.spring.rms.interfaces.webapi;

import org.springframework.boot.CommandLineRunner;

import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.StartupLog;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StartupLogRunner implements CommandLineRunner {

    private final MainModuleInformation moduleInfo;

    @Override
    public void run(String... args) throws Exception {
        StartupLog.startupLog(moduleInfo);
    }
}
