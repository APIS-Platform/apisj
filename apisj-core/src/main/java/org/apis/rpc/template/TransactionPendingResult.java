package org.apis.rpc.template;

public class TransactionPendingResult {
    private boolean isValid = false;
    private String err = null;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }
}
