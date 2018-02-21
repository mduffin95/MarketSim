import desmoj.core.simulator.Model;

public class ZIP extends TradingAgent {
    private double mu = 0.0;
    private double ca = 0.05;
    private double cr = 0.05;

    private double learning_rate;
    private LOBSummary currentSummary;
    private Direction direction;


    public ZIP(Model model, int limit, Exchange e, SecuritiesInformationProcessor sip, Direction direction) {
        super(model, limit, e, sip);
        this.direction = direction;
        learning_rate = 0.25;
    }

    @Override
    public void doSomething() {
        return;
    }

    @Override
    protected void respond(MarketUpdate update) {
        //Determine what has happened
        LOBSummary newSummary = update.summary;
        Trade trade = update.trade;

        boolean deal = trade != null;
        Direction lastOrderDirection = null;
        int price;


        if (currentSummary.getBestBuyOrder() != newSummary.getBestBuyOrder()) {
            //Either new buy order or trade occurred that cleared with the buy order
            if (deal) {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = trade.price;
            } else {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = newSummary.getBestBuyOrder().getPrice();
            }
        } else if (currentSummary.getBestSellOrder() != newSummary.getBestSellOrder()) {
            //Either new sell order or trade occurred that cleared with the sell order
            if (deal) {
                //Most recent order was a buy order
                lastOrderDirection = Direction.BUY;
                price = trade.price;
            } else {
                //Most recent order was a sell order
                lastOrderDirection = Direction.SELL;
                price = newSummary.getBestSellOrder().getPrice();
            }
        } else {
            //Nothing has changed
            return;
        }

        currentSummary = newSummary;

        int target;
        if (direction == Direction.SELL) {
            if (deal)  {
                //Trade has occurred.
                if (getPrice() <= trade.price) {
                    //increase profit margin
                    target = target_up(price);
                    updateMargin(target);
                } else if (active) {
                    //Being undercut - reduce profit margin
                    target = target_down(price);
                    updateMargin(target);
                }
            } else if (active && lastOrderDirection == Direction.SELL && getPrice() > price) {
                //Being undercut - reduce profit margin
                target = target_down(price);
                updateMargin(target);
            }

        } else {
            if (deal) {
                //Trade has occurred.
                if (getPrice() >= trade.price) {
                    //increase profit margin (lower bid price)
                    target = target_down(price);
                    updateMargin(target);
                } else if (active) {
                    //Price too low - reduce profit margin
                    target = target_up(price);
                    updateMargin(target);
                }
            } else if (active && lastOrderDirection == Direction.BUY && getPrice() < price) {
                //Price too low - reduce profit margin
                target = target_up(price);
                updateMargin(target);
            }
        }
    }

    private int target_up(int price) {
        double ptrb_abs = ca * marketSimModel.generator.nextDouble();
        double ptrb_rel = (1 + cr * marketSimModel.generator.nextDouble()) * price;

        return (int)Math.round(ptrb_rel + ptrb_abs);

    }

    private int target_down(int price) {
        double ptrb_abs = ca * marketSimModel.generator.nextDouble();
        double ptrb_rel = (1 - cr * marketSimModel.generator.nextDouble()) * price;

        return (int)Math.round(ptrb_rel - ptrb_abs);

    }

    private void updateMargin(int target) {
        int price = getPrice();
        int delta = getDelta(target, price);
        mu = (price * delta) / limit - 1.0;
    }

    private int getDelta(int target, int price) {
        return (int)Math.round(learning_rate * (target - price));
    }

    private int getPrice() {
        return (int)Math.round(limit * (1 + mu));
    }
}
