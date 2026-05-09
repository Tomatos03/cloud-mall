# Module Architecture

```
cloud-mall (pom aggregator)
├── cloud-mall-framework    Core domain models, services, configs (library, not runnable)
├── cloud-mall-web          Customer-facing API       (port 7001, CloudMallWebApplication)
├── cloud-mall-manager      Admin/management API      (port 7000, CloudMallManageApplication)
├── cloud-mall-merchant     Merchant portal API       (port 7002, CloudMallMerchantApplication)
├── cloud-mall-aggregation  Combined startup module   (port 7777, all 3 apps merged)
└── im                       Instant messaging         (port 7010, IMApplication)
```

`cloud-mall-framework` contains ~350 Java files with all business logic. The app modules (web/manager/merchant) are thin shells: controllers + security filter chain + config. Controllers delegate to framework services.
