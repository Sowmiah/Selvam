package com.myfirstproject.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class OrderUpdate {
    @JsonProperty("norenordno")
    private String norenOrderNo;

    @JsonProperty("uid")
    private String userId;

    @JsonProperty("actid")
    private String accountId;

    @JsonProperty("exch")
    private String exchange;

    @JsonProperty("tsym")
    private String tradingSymbol;

    @JsonProperty("qty")
    private Integer quantity;

    @JsonProperty("prc")
    private BigDecimal price;

    @JsonProperty("prd")
    private String product;

    @JsonProperty("status")
    private String status;

    @JsonProperty("reporttype")
    private String reportType;

    @JsonProperty("trantype")
    private String transactionType;

    @JsonProperty("prctyp")
    private String priceType;

    @JsonProperty("fillshares")
    private Integer filledShares;

    @JsonProperty("avgprc")
    private BigDecimal averagePrice;

    @JsonProperty("exchordid")
    private String exchangeOrderId;

    @JsonProperty("remarks")
    private String remarks;

    // Getters and Setters
    public String getNorenOrderNo() {
        return norenOrderNo;
    }

    public void setNorenOrderNo(String norenOrderNo) {
        this.norenOrderNo = norenOrderNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Integer getFilledShares() {
        return filledShares;
    }

    public void setFilledShares(Integer filledShares) {
        this.filledShares = filledShares;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "OrderUpdate{" +
                "norenOrderNo='" + norenOrderNo + '\'' +
                ", userId='" + userId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", exchange='" + exchange + '\'' +
                ", tradingSymbol='" + tradingSymbol + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", reportType='" + reportType + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", filledShares=" + filledShares +
                '}';
    }
}
