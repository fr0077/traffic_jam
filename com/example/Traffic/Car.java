package com.example.Traffic;

import com.sun.istack.internal.Nullable;

/**
 * 車クラス.
 * Created by Ryohei Fujii on 2016/05/19.
 */
public class Car {
    //前回生成した車のID
    private static int previousID = -1;
    /**
     * 車を識別するID
     */
    public final int id;
    private final TrafficManager manager;
    //前が空いている時に進む最大の距離
    private int speed = 1;
    private Rule rule;
    private boolean isRuleSustainable = false;
    private boolean isNextFilled = false;
    private boolean wasNextFilled = false;

    /**
     * 車を生成する.
     *
     * @param manager この車を管理するTrafficManager
     */
    Car(TrafficManager manager) {
        id = previousID + 1;
        previousID++;
        this.manager = manager;
    }

    /**
     * 走っている道を取得する.
     *
     * @return 走っている道
     */
    public Road getRoad() {
        return manager.getRoad(this);
    }

    /**
     * 移動規則が設定されているかどうか返す
     *
     * @return 移動規則が設定されているかどうか
     */
    public boolean isRuleSet() {
        return rule != null;
    }

    /**
     * 車線変更後もルールを維持するかどうか設定する
     *
     * @param isRuleSustainable 車線変更後もルールを維持するかどうか
     */
    public void setIsRuleSustainable(boolean isRuleSustainable) {
        this.isRuleSustainable = isRuleSustainable;
    }

    /**
     * 車線変更後もルールを維持するかどうか取得する.
     *
     * @return 車線変更後もルールを維持するかどうか
     */
    public boolean isRuleSustainable() {
        return isRuleSustainable;
    }

    /**
     * 車に移動規則を設定する. この規則はTrafficManagerよりも道路の規則よりも優先する.
     *
     * @param rule 規則
     */
    public void addRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * 移動規則を削除する.
     */
    public void removeRule() {
        this.rule = null;
    }

    /**
     * 移動規則を適用するためのpackage privateなメソッド.
     * 外部から呼ぶ際はpublic move(int length)を呼ぶ.
     *
     * @param rule              上位（道またはManager）のルール
     * @param left              左の車線
     * @param right             右の車線
     * @param forwardFreeLength forwardFreeLength
     * @param backFreeLength    後ろの車間距離
     * @param nextJamLength     次の渋滞までの距離
     */
    void move(
            Rule rule,
            @Nullable Road left,
            @Nullable Road right,
            int forwardFreeLength,
            int backFreeLength,
            int nextJamLength) {
        if (this.rule == null && rule == null) {
            //上位のルールが存在せず, かつ車が固有のルールをもたない場合. デフォルトの動作.
            manager.moveCar(this, speed);
        } else if (this.rule == null) {
            //上位のルールが存在し, かつ車が固有のルールをもたない場合.
            rule.onMove(this, left, right, forwardFreeLength, backFreeLength, nextJamLength, wasSecondOfJam());
        } else {
            //車が固有のルールを持つ場合
            this.rule.onMove(this, left, right, forwardFreeLength, backFreeLength, nextJamLength, wasSecondOfJam());
        }
    }

    /**
     * 1ステップ前に渋滞の2番めの車だったかどうか返す.
     *
     * @return 1ステップ前に渋滞の2番めの車だったかどうか
     */
    private boolean wasSecondOfJam() {
        Road road = getRoad();
        wasNextFilled = isNextFilled;
        isNextFilled = road.isFilled(road.positionOf(this) + 1);
        return wasNextFilled && !isNextFilled;
    }

    /**
     * 指定した長さだけ進む. lengthが大きすぎてそれだけ進めない場合, 進める長さに丸められる.
     *
     * @param length 長さ
     */
    public void move(int length) {
        manager.moveCar(this, length);
    }

    /**
     * 車線を変更する.
     * 今回は使用せず.
     *
     * @param to 移動先の道
     * @return 成功／失敗
     */
    public boolean changeRoad(Road to) {
        return manager.changeRoad(this, to);
    }

    /**
     * この車の左の車線を返す
     *
     * @return 左の車線
     */
    Road left() {
        return getRoad().left();
    }

    /**
     * この車の右の車線を返す
     *
     * @return 右の車線
     */
    Road right() {
        return getRoad().right();
    }

    /**
     * 車のスピードを取得する
     *
     * @return スピード
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * 車のスピード（前が空いている時に進む最大の距離）を設定する.
     *
     * @param speed スピード
     */
    public void setSpeed(int speed) {
        if (this.speed < 0) {
            speed = 0;
        }

        this.speed = speed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Car other = (Car) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    /**
     * 車の移動規則を定義するクラス.
     */
    public abstract static class Rule {
        /**
         * 移動規則.
         * <b>最後に必ずsuper.onMoveを呼ぶ必要があります.</b>
         *
         * @param car            ルールの対象となる, 現在走っている車
         * @param left           今走っている車線の1つ左の車線.
         * @param right          今走っている車線の1つ右の車線.
         * @param freeLength     今走っている位置より先の空いている道路の長さ.
         * @param backLength     今走っている位置より後ろの空いている道路の長さ.
         * @param nextJam        次の渋滞までの距離. ■□□■■なら2となる.
         * @param wasSecondOfJam 1ステップ前に渋滞の2番めの車だったかどうか
         */
        public void onMove(Car car,
                           @Nullable Road left,
                           @Nullable Road right,
                           int freeLength,
                           int backLength,
                           int nextJam,
                           boolean wasSecondOfJam) {
            if (!car.isRuleSustainable) {
                car.addRule(car.getRoad().getRule());
            }
        }
    }
}
