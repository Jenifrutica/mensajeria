package courier.ui;

import courier.service.OperationResult;

import java.util.function.Supplier;

public interface ScreenHost {
    OperationResult run(Supplier<OperationResult> operation);

    void refreshAll();
}
