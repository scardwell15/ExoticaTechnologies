package exoticatechnologies.dialog.modifications;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;

@Log4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemOptionsHandler {
    @Getter
    private static List<SystemState> validSystemsOptions = new ArrayList<>();

    public static void addOption(SystemState state) {
        validSystemsOptions.add(state);
        log.info(String.format("[ET] Registered system option [%s]", state.getClass().getSimpleName()));
    }
}
