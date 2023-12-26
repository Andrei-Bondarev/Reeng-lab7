package sample;

import java.util.*;
import java.text.*;

/** Containing items and calculating price. */
public class ShoppingCart{

    public static enum ItemType { NEW, REGULAR, SECOND_FREE, SALE };
    /**
     * Container for added items 
     */
    private List<Item> items = new ArrayList<Item>();

    /**
     * Tests all class methods. 
     */
    public static void main(String[] args) {
        // TODO: add tests here
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Apple", 0.99, 5, ItemType.NEW);
        cart.addItem("Banana", 20.00, 4, ItemType.SECOND_FREE);
        cart.addItem("A long piece of toilet paper", 17.20, 1, ItemType.SALE);
        cart.addItem("Nails", 2.00, 500, ItemType.REGULAR);
        System.out.println(cart.formatTicket());
    }

    /** Adds new item.
     * @param title item title 1 to 32 symbols
     * @param price item ptice in USD, > 0
     * @param quantity item quantity, from 1
     * @param type item type
     *
     * @throws IllegalArgumentException if some value is wrong 
     */
    public void addItem(String title, double price, int quantity, ItemType type){

        if (title == null || title.isEmpty() || title.length() > 32)
            throw new IllegalArgumentException("Illegal title");

        if (price < 0.01)
            throw new IllegalArgumentException("Illegal price");

        if (quantity <= 0)
            throw new IllegalArgumentException("Illegal quantity");

        Item item = new Item();
        item.setTitle(title);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setType(type);
        items.add(item);

    }

    /**
     * Formats shopping price.
     * @return string as lines, separated with \n,
     *    first line: # Item    Price Quan. Discount  Total 
     *    second line: ---------------------------------------
     *    next lines: NN Title     $PP.PP  Q    DD%  $TT.TT
     *            1 Some title     $.30    2    -    $.60
     *            2 Some very long $100.00 1    50%  $50.00
     *            ...
     *            31 Item 42       $999.00 1000 -    $999000.00
     *    end line: -------------------------------------------
     *    last line: 31                              $999050.60
     * if no items in cart returns "No items." string.
     */
    private List<String[]> convertItemsToTableLines(){

        List<String[]> lines = new ArrayList<String[]>();
        int index = 0;
        for (Item item : items) {
            int discount = calculateDiscount(item.type, item.quantity);
            lines.add(new String[]{
                    String.valueOf(++index),
                    item.getTitle(),
                    MONEY.format(item.getPrice()),
                    String.valueOf(item.getQuantity()),
                    (discount == 0) ? "-" : (discount + "%"),
                    MONEY.format(calculateItemTotal(item))
            });
        }
        return lines;
    }

    private double calculateItemsParameters() {
        double total = 0.00;
        for (Item item : items) {
            total += calculateItemTotal(item);
        }
        return total;
    }
    private double calculateItemTotal(Item item) {
        return item.price * item.quantity * (100.00 - calculateDiscount(item.type, item.quantity)) / 100.00;
    }

    public String getFormattedTicketTable(double total){
        if (items.size() == 0)
            return "No items.";
        String[] header = {"#","Item","Price","Quan.","Discount","Total"};
        int[] align = new int[] { 1, -1, 1, 1, 1, 1 };
        int index = 0;
        List<String[]> lines = convertItemsToTableLines();
        String[] footer = { String.valueOf(index),
                "","","","",
                MONEY.format(total) };
        // formatting table
        // column max length
        int[] width = new int[]{0,0,0,0,0,0};
        for (String[] line : lines)
            adjustColumnWidth(width, line);
        adjustColumnWidth(width, header);
        adjustColumnWidth(width, footer);

        // line length
        int lineLength = 1;
        StringBuilder sb = new StringBuilder();

        // header
        appendFormattedLine(sb, header, align, width, true);
        appendSeparator(sb, lineLength); // lines
        for (String[] line : lines) {
            appendFormattedLine(sb, line, align, width, true);
            appendSeparator(sb, lineLength);
        }

        // footer
        appendFormattedLine(sb, footer, align, width, false);
        return sb.toString();
    }
    public String formatTicket(){
        return getFormattedTicketTable(calculateItemsParameters());
    }



    // --- private section -----------------------------------------------------
    private static final NumberFormat MONEY;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        MONEY = new DecimalFormat("$#.00", symbols);

    }

    /** Appends to sb formatted value.
     *  Trims string if its length > width.
     * @param align -1 for align left, 0 for center and +1 for align right. 
     */
    public static void appendFormatted(StringBuilder sb, String value, int align, int width){
        if (value.length() > width)
            value = value.substring(0,width);
        int before = (align == 0)
                ? (width - value.length()) / 2
                : (align == -1) ? 0 : width - value.length();
        int after = width - value.length() - before;
        while (before-- > 0)
            sb.append(" ");
        sb.append(value);
        while (after-- > 0)
            sb.append(" ");
        sb.append(" ");
    }

    /**
     * Calculates item's discount.
     * For NEW item discount is 0%;
     * For SECOND_FREE item discount is 50% if quantity > 1
     * For SALE item discount is 70%
     * For each full 10 not NEW items item gets additional 1% discount,
     * but not more than 80% total
     */
    public static int calculateDiscount(ItemType type, int quantity){
        int discount = 0;
        switch (type) {
            case NEW:
                return 0;
            case REGULAR:
                discount = 0;
                break;
            case SECOND_FREE:
                if (quantity > 1)
                    discount = 50;
                break;
            case SALE:
                discount = 70;
                break;
        }
        if (discount < 80) {
            discount += quantity / 10;
            if (discount > 80)
                discount = 80;
        }
        return discount;
    }
    private void appendSeparator(StringBuilder sb, int lineLength){
        for(int i = 0; i < lineLength; i++)
            sb.append("\n");
    }

    private void adjustColumnWidth(int[] width, String[] columns){
        for(int i = 0; i < width.length; i++)
            width[i] = (int) Math.max(width[i], columns[i].length());
    }

    private void appendFormattedLine(StringBuilder sb,
                                     String[] line,
                                     int[] align,
                                     int[] width,
                                     Boolean newLine){
        for(int i = 0; i < line.length; i++)
            appendFormatted(sb, line[i], align[i], width[i]);
        if(newLine)
            sb.append("\n");
    }
    /** item info */
    private static class Item {

        private String title;
        private double price;
        private int quantity;
        private ItemType type;
        private int discount;
        private double total;

        public String getTitle(){
            return title;
        }

        public void setTitle(String title){
            this.title = title;
        }

        public double getPrice(){
            return price;
        }
        public void setPrice(double price){
            this.price = price;
        }

        public int getQuantity(){
            return quantity;
        }

        public void setQuantity(int quantity){
            this.quantity = quantity;
        }

        public ItemType getItemType(){
            return type;
        }

        public void setItemType(ItemType type){
            this.type = type;
        }


        public void setDiscount(int discount){
            this.discount = discount;
        }

        public int getDiscount(){
            return discount;
        }

        public double getTotalPrice(){
            return total;
        }

        public void setTotalPrice(double total){
            this.total = total;
        }

        public void setType(ItemType type) {
            this.type = type;
        }

        public ItemType getType() {
            return type;
        }
    }
}