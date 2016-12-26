package com.example.Traffic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

/**
 * 道路クラス.
 * Created by Ryohei Fujii on 2016/05/19
 */
public class Road {
    /**
     * 道の長さ
     */
    public final int length;
    private final TrafficManager manager;
    //車のIDと位置を保持
    private HashMap<Car, Integer> carList;
    private HashMap<Car, Integer> carListBuffer;
    private Car.Rule rule;

    /**
     * コンストラクタ.
     * 空の道路を初期化する.
     *
     * @param length  道路長
     * @param manager この道を管理するTrafficManager
     */
    Road(int length, TrafficManager manager) {
        if (length < 0)
            throw new IllegalArgumentException("length must be bigger than or equal to 0.");

        carList = new HashMap<>(length);
        carListBuffer = new HashMap<>(length);

        this.length = length;
        this.manager = manager;
    }

    /**
     * コンストラクタ.
     * 文字列から道路と車を初期化する.
     * 車：■または1
     * 空き：□または0
     * これら以外の文字は無視される.
     *
     * @param roadCondition 文字列.
     * @param manager       この道を管理するTrafficManager
     */
    Road(String roadCondition, TrafficManager manager) {
        if (roadCondition == null)
            roadCondition = "";

        StringBuilder cleanedRoadCondition = new StringBuilder();
        for (int i = 0; i < roadCondition.length(); i++) {
            char roadChar = roadCondition.charAt(i);
            if (roadChar == '■'
                    || roadChar == '□'
                    || roadChar == '1'
                    || roadChar == '0') {
                cleanedRoadCondition.append(roadChar);
            }
        }

        length = cleanedRoadCondition.length();
        this.manager = manager;
        carList = new HashMap<>(length);
        carListBuffer = new HashMap<>(length);

        for (int i = 0; i < cleanedRoadCondition.length(); i++) {
            if (cleanedRoadCondition.charAt(i) == '■'
                    || cleanedRoadCondition.charAt(i) == '1') {
                addCar(new Car(this.manager), i);
            }
        }
        reflesh();
    }

    /**
     * 車の配列を返す.
     *
     * @return 車の配列
     */
    public Car[] getCars() {
        Set<Car> carSet = carList.keySet();
        return carSet.toArray(new Car[carSet.size()]);
    }

    /**
     * 指定した車がこの道を走っているかどうか返す.
     *
     * @param car 車
     * @return 走っているならtrue, いなければfalse
     */
    public boolean contains(Car car) {
        return carListBuffer.containsKey(car);
    }

    /**
     * この道路上のすべての車のスピードを設定する.
     *
     * @param speed スピード
     */
    public void setSpeed(int speed) {
        Car[] cars = getCars();
        for (Car car : cars) {
            car.setSpeed(speed);
        }
    }

    /**
     * 車を走らせる.
     *
     * @param rule 上位のルール
     */
    public void update(Car.Rule rule) {
        Car[] cars = getCars();

        Car.Rule currentRule;

        if (this.rule == null && rule == null) {
            currentRule = null;
        } else if (this.rule != null) {
            currentRule = this.rule;
        } else {
            currentRule = rule;
        }

        for (Car car : cars) {
            car.move(currentRule, left(), right(), forwardFreeLength(car), backFreeLength(car), nextJamLength(car));
        }
        reflesh();
    }

    public boolean isFilled(int position) {
        ArrayList<Integer> sortedPositions = getSortedPositions();
        return sortedPositions.contains(normalize(position));
    }

    /**
     * この道路の左の車線を返す.
     *
     * @return 左の車線
     */
    Road left() {
        return manager.left(this);
    }

    /**
     * この道路の右の車線を返す.
     *
     * @return 右の車線
     */
    Road right() {
        return manager.right(this);
    }

    /**
     * この道路を走っている車に移動規則を適用する.
     * 車固有のルールのほうが優先される
     *
     * @param rule 規則
     */
    public void addRule(Car.Rule rule) {
        Car[] cars = getCars();

        for (Car car : cars) {
            car.addRule(rule);
        }
    }

    /**
     * この車線の移動ルールを返す.
     *
     * @return 移動ルール
     */
    Car.Rule getRule() {
        return rule;
    }

    /**
     * carListBufferをcarListに反映
     */
    public void reflesh() {
        carList.clear();
        carList.putAll(carListBuffer);
    }

    /**
     * バッファー内の指定した場所が埋まっているか確認する
     *
     * @param position 場所
     * @return 埋まっていればtrue, いなければfalse.
     */
    private boolean isBufferFilled(int position) {
        position = normalize(position);

        return carListBuffer.containsValue(position);
    }

    /**
     * 指定した車の位置を返す
     *
     * @param car 車
     * @return 位置
     */
    public int positionOf(Car car) {
        if (contains(car)) {
            return carListBuffer.get(car);
        } else {
            return -1;
        }
    }

    /**
     * 指定した車を道路から消す.
     * 既に車がない場合は無視される.
     *
     * @param car 車
     */
    public void removeCar(Car car) {
        carListBuffer.remove(car);
    }

    /**
     * 指定した車を指定した位置に追加する.
     *
     * @param car      車
     * @param position 位置
     * @return 成功(true)／失敗(false)
     */
    public boolean addCar(Car car, int position) {
        position = normalize(position);

        //すでに同じ車が走っている時
        if (contains(car))
            throw new IllegalArgumentException("This car is already running on this road.");


        if (isBufferFilled(position)) {
            return false;
        }

        carListBuffer.put(car, position);
        return true;
    }

    /**
     * 現時点から移動可能なセルの長さを返す
     *
     * @param car 対象となる車
     * @return 移動可能なセルの数
     */
    public int forwardFreeLength(Car car) {
        if (!contains(car))
            throw new IllegalArgumentException("This car is not running on this road.");

        //車の位置
        int position = carList.get(car);
        //車の位置のインデックス
        int positionIndex;

        ArrayList<Integer> sortedPositions = getSortedPositions();
        positionIndex = sortedPositions.indexOf(position);

        int nextPositionIndex;
        if (positionIndex + 1 == sortedPositions.size()) {
            nextPositionIndex = 0;
        } else {
            nextPositionIndex = positionIndex + 1;
        }

        int nextPosition = sortedPositions.get(nextPositionIndex);
        if (nextPosition > position) {
            return nextPosition - position - 1;
        } else if (nextPosition == position) {
            return 0;
        } else {
            return length - (position - nextPosition + 1);
        }
    }

    /**
     * 指定した車の後ろの車間距離を返す.
     *
     * @param car 車
     * @return 後ろの車間距離
     */
    public int backFreeLength(Car car) {
        if (!contains(car))
            throw new IllegalArgumentException("This car is not running on this road.");

        //車の位置
        int position = carList.get(car);
        //車の位置のインデックス
        int positionIndex;

        ArrayList<Integer> sortedPositions = getSortedPositions();
        positionIndex = sortedPositions.indexOf(position);

        int beforePositionIndex;
        if (positionIndex - 1 == -1) {
            beforePositionIndex = sortedPositions.size() - 1;
        } else {
            beforePositionIndex = positionIndex - 1;
        }

        int beforePosition = sortedPositions.get(beforePositionIndex);
        if (beforePosition < position) {
            return position - beforePosition - 1;
        } else if (beforePosition == position) {
            return 0;
        } else {
            return length - (beforePosition - position + 1);
        }
    }

    /**
     * 指定した車の次の渋滞までの距離を返す.
     * ■□□■■なら2となる.
     *
     * @param car 車
     * @return 次の渋滞までの距離. 0なら渋滞の中. Integer.MAX_VALUEなら渋滞が存在しない.
     */
    public int nextJamLength(Car car) {
        if (!contains(car))
            throw new IllegalArgumentException("This car is not running on this road.");

        ArrayList<Integer> sortedPositions = getSortedPositions();
        final int index = sortedPositions.indexOf(positionOf(car));

        int nextJam = Integer.MAX_VALUE;

        for (int i = 0; i < sortedPositions.size(); i++) {
            int normalizedIndex = index + i;
            int normalizedIndexP = index + i + 1;
            if (normalizedIndex >= sortedPositions.size())
                normalizedIndex -= sortedPositions.size();
            if (normalizedIndexP >= sortedPositions.size())
                normalizedIndexP -= sortedPositions.size();

            int position = sortedPositions.get(normalizedIndex);
            int positionP = sortedPositions.get(normalizedIndexP);

            if (normalize(position + 1) == normalize(positionP)) {
                nextJam = i - 1;
                break;
            }
        }

        if (sortedPositions.contains(normalize(positionOf(car) - 1))) {
            nextJam = 0;
        }

        return nextJam;
    }

    /**
     * 道の状態を出力する.
     *
     * @return 車あり：■, 車なし：□
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> sortedPositions = getSortedPositions();

        int j = 0;
        for (int i = 0; i < length; i++) {
            if (j < sortedPositions.size()) {
                if (i == sortedPositions.get(j)) {
                    stringBuilder.append("■");
                    j++;
                } else {
                    stringBuilder.append("□");
                }
            } else {
                stringBuilder.append("□");
            }
        }

        return stringBuilder.toString();
    }

    /**
     * ソートされた位置のArrayListを返す.
     *
     * @return ソートされた位置のArrayList
     */
    private ArrayList<Integer> getSortedPositions() {
        ArrayList<Integer> values = new ArrayList<>(carList.values());
        values.sort(Comparator.naturalOrder());
        return values;
    }

    /**
     * 周期境界条件を課し, positionを正規化する.
     *
     * @param position 正規化前のposition.
     * @return 正規化後のposition.
     */
    private int normalize(int position) {
        if (position < 0) {
            do {
                position += length;
            } while (position <= 0);
        }
        return position % length;
    }
}
