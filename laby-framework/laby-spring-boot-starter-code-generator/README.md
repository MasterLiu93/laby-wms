# laby-spring-boot-starter-code-generator

业务编码生成器，支持分布式环境下的唯一编码生成。

## 功能特性

- **分布式唯一性**：基于 Redis 的原子操作，确保在分布式环境下的编码唯一性
- **灵活的编码规则**：支持自定义前缀、日期格式和序号长度
- **每日重置**：业务单据（如入库单、出库单）的序号每日自动重置
- **批量生成**：提供批量生成接口，优化性能

## 快速使用

### 1. 引入依赖

在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.laby.boot</groupId>
    <artifactId>laby-spring-boot-starter-code-generator</artifactId>
</dependency>
```

### 2. 配置 Redis

确保您的 `application.yml` 或 `application-dev.yml` 中配置了 Redis 连接信息。

### 3. 注入并使用 `CodeGeneratorService`

在您的业务 Service 中，可以直接注入 `CodeGeneratorService` 并调用其方法：

```java
@Service
public class WmsInboundService {

    @Resource
    private CodeGeneratorService codeGeneratorService;

    /**
     * 创建入库单时生成入库单号
     */
    public String createInboundOrder() {
        // 生成入库单号，使用 CodePrefixEnum.INBOUND
        String inboundNo = codeGeneratorService.generateCode(CodePrefixEnum.INBOUND);
        // ... 业务逻辑
        return inboundNo;
    }

    /**
     * 批量生成商品 SKU 编码
     */
    public List<String> generateSkuCodes(int count) {
        // 批量生成 SKU 编码，使用 CodePrefixEnum.GOODS_SKU
        List<String> skuCodes = codeGeneratorService.generateBatchCode(CodePrefixEnum.GOODS_SKU, count);
        // ... 业务逻辑
        return skuCodes;
    }
}
```

### 4. 定义编码前缀

您可以在 `com.laby.framework.codegen.core.enums.CodePrefixEnum` 中查看所有已定义的业务编码前缀。

## API 文档

### CodeGeneratorService

#### 生成单个编码

```java
String generateCode(CodePrefixEnum codePrefix)
```

生成一个业务编码。

**参数：**
- `codePrefix` - 编码前缀枚举，定义在 `CodePrefixEnum` 中

**返回值：**
- 生成的编码字符串（格式：前缀 + 日期(yyyyMMdd) + 序号）

**示例：**
```java
String inboundNo = codeGeneratorService.generateCode(CodePrefixEnum.INBOUND);
// 结果：IN20251030001（表示2025年10月30日的第1个入库单）
```

#### 批量生成编码

```java
List<String> generateBatchCode(CodePrefixEnum codePrefix, int count)
```

批量生成业务编码，性能优于循环调用单个生成。

**参数：**
- `codePrefix` - 编码前缀枚举
- `count` - 生成数量

**返回值：**
- 生成的编码列表

**示例：**
```java
List<String> skuCodes = codeGeneratorService.generateBatchCode(CodePrefixEnum.GOODS_SKU, 10);
// 结果：[SKU20251030001, SKU20251030002, ..., SKU20251030010]
```

## 支持的编码类型

| 编码类型 | 前缀 | 说明 | 格式示例 |
|---------|------|------|---------|
| 入库单 | IN | 入库业务单据号 | IN20251030001 |
| 出库单 | OUT | 出库业务单据号 | OUT20251030001 |
| 商品SKU | SKU | 商品唯一标识码 | SKU20251030001 |
| 仓库 | WH | 仓库编码 | WH20251030001 |
| 库区 | WA | 库区编码 | WA20251030001 |
| 库位 | LOC | 库位编码 | LOC20251030001 |
| 供应商 | SUP | 供应商编码 | SUP20251030001 |
| 客户 | CUS | 客户编码 | CUS20251030001 |
| 拣货波次 | WAVE | 拣货波次号 | WAVE20251030001 |
| 拣货任务 | PICK | 拣货任务号 | PICK20251030001 |
| 盘点单 | ST | 盘点单号 | ST20251030001 |
| 移库单 | MOVE | 移库单号 | MOVE20251030001 |

完整列表请查看 `CodePrefixEnum` 枚举类。

## 技术实现

- **核心技术**：Redis INCR 命令（原子操作）
- **线程安全**：完全线程安全，支持高并发
- **分布式支持**：多实例环境下保证编码唯一性
- **性能优化**：批量生成时一次性获取序号区间

## 注意事项

1. 确保 Redis 服务正常运行
2. 编码序号每日自动重置（通过设置 Redis key 的过期时间实现）
3. 序号默认为 3 位，范围 001-999
4. 如需修改编码规则，请参考 `CodePrefixEnum` 进行扩展
