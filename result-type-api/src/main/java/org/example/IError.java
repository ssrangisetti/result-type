package org.example;

public interface IError {
    IError source();
    String message();
    IError context(String ctx);
}
