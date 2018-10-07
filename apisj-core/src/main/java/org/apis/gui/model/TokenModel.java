package org.apis.gui.model;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import org.apis.gui.common.IdenticonGenerator;
import org.apis.gui.model.base.BaseModel;

import java.io.IOException;
import java.math.BigInteger;

public class TokenModel extends BaseModel {
    private String tokenAddress;
    private String tokenName;
    private String tokenSymbol;

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

}
