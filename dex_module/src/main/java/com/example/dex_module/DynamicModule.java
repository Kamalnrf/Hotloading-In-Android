package com.example.dex_module;

interface IDynamicModule {
    String getText();
}

public class DynamicModule implements IDynamicModule{

    @Override
    public String getText() {
        return "Hello from dynamic module";
    }
}
