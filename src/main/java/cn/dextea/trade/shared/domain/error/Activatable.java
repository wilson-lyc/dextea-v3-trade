package cn.dextea.trade.shared.domain.error;

public interface Activatable {
    boolean isActive();

    BizErrorCode inactiveErrorCode();
}
