package org.example;

public record StdError(String message, IError source) implements IError {
    @Override
    public String message() {
        if (message != null) return message;
        return source != null ? source.message() : null;
    }

    @Override
    public IError context(String ctx) {
        return new StdError(ctx, this);
    }

    public static StdError from(IError err) {
        return switch (err) {
            case StdError stdError -> stdError;
            case null -> new StdError(null, null);
            default -> new StdError(null, err);
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(message());
        IError src = source;
        if (src != null) {
            sb.append("\nCaused By:");
        }
        while (src != null) {
            if (src.message() != null) sb.append("\n\t").append(src.message());
            src = src.source();
        }
        return sb.toString();
    }
}
