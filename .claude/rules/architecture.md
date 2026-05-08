# Module Architecture

```
online-mall (pom aggregator)
├── online-shop-framework    Core domain models, services, configs (library, not runnable)
├── online-shop-web          Customer-facing API       (port 7001, CloudMallWebApplication)
├── online-shop-manager      Admin/management API      (port 7000, CloudMallManageApplication)
├── online-shop-merchant     Merchant portal API       (port 7002, CloudMallMerchantApplication)
├── online-shop-aggregation  Combined startup module   (port 7777, all 3 apps merged)
└── im                       Instant messaging         (port 7010, IMApplication)
```

`online-shop-framework` contains ~350 Java files with all business logic. The app modules (web/manager/merchant) are thin shells: controllers + security filter chain + config. Controllers delegate to framework services.
