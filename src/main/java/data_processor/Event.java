package data_processor;

class Event {
    public static final String STATUS_DECLINED = "DECLINED";
    public static final String STATUS_APPROVED = "APPROVED";

    public String transactionId;
    public String status;
    public String message;

    Event(String transactionId, String status, String message ){
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;

    }
}