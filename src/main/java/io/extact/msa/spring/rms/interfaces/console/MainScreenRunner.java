package io.extact.msa.spring.rms.interfaces.console;

import static io.extact.msa.spring.rms.interfaces.console.common.ClientConstants.*;

import org.springframework.boot.CommandLineRunner;

import io.extact.msa.spring.platform.core.env.MainModuleInformation;
import io.extact.msa.spring.platform.fw.StartupLog;
import io.extact.msa.spring.platform.fw.exception.RmsServiceUnavailableException;
import io.extact.msa.spring.rms.interfaces.console.screen.MainScreenController;
import io.extact.msa.spring.rms.interfaces.console.textio.TextIoUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * コンソールアプリの実質的なエントリポイント。
 */
@Slf4j
public class MainScreenRunner implements CommandLineRunner {

    private static final String START_UP_LOGO = """
                ____    __  ___  _____
               / __ \\  /  |/  / / ___/
              / /_/ / / /|_/ /  \\__ \\
             / _, _/ / /  / /_ ___/ /
            /_/ |_(_)_/  /_/(_)____(_)
            """;
    private final MainScreenController mainController;

    public MainScreenRunner(MainScreenController mainController, MainModuleInformation info) {

        this.mainController = mainController;

        StartupLog.startupLog(info);
        TextIoUtils.println(START_UP_LOGO);
    }

    @Override
    public void run(String... args) throws Exception {

        try {
            while (true) {
                try {
                    mainController.start();
                    break;
                } catch (RmsServiceUnavailableException e) {
                    log.warn(e.getMessage());
                    TextIoUtils.printErrorInformation(SERVICE_UNAVAILABLE_INFORMATION);
                } catch (Exception e) {
                    log.error("Back to start..", e);
                    TextIoUtils.printErrorInformation(UNKNOWN_ERROR_INFORMATION);
                }
            }

            // TODO プロセスの終了方法を確認
            // swingコンソールはmainプロセスが残るためexitする
            //System.exit(0);

        } catch (Throwable e) { // TODO:これって必要か？
            log.error("error occured.", e);
            throw e;
        }
    }

}
