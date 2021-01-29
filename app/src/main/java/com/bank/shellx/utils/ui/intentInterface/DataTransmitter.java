package com.bank.shellx.utils.ui.intentInterface;

@FunctionalInterface
public interface DataTransmitter<T> {

    void onTransfer(T t);

}

