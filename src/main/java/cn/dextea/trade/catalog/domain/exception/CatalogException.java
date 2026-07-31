package cn.dextea.trade.catalog.domain.exception;

import cn.dextea.trade.shared.domain.error.BizError;

public class CatalogException extends BizError {
    public CatalogException(CatalogErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }

    public CatalogException(CatalogErrorCode errorCode, String detail) {
        super(errorCode.getCode(), errorCode.getMessage() + ": " + detail);
    }
}
