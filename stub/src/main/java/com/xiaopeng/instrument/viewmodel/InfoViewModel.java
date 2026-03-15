package com.xiaopeng.instrument.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.xiaopeng.instrument.bean.InfoBean;
import com.xiaopeng.instrument.bean.InfoContainBean;

import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class InfoViewModel extends ViewModel  {

    public InfoViewModel() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Integer> getLeftListIndexLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Integer> getRightListIndexLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Boolean> getLeftCardLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Boolean> getRightCardLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Integer> getLeftSubCardLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Integer> getRightSubCardLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Boolean> getLeftListInfoLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Boolean> getRightListInfoLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<InfoContainBean> getLeftListLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<InfoContainBean> getRightListLiveData() {
        throw new RuntimeException("Stub!");
    }

    public MutableLiveData<Integer> getGearLiveData() {
        throw new RuntimeException("Stub!");
    }

    @Override // androidx.lifecycle.ViewModel
    protected void onCleared() {
        throw new RuntimeException("Stub!");
    }

    private void initInfoData() {
        throw new RuntimeException("Stub!");
    }

    private void showSubCard(int i, int i2) {
        throw new RuntimeException("Stub!");
    }

    private void updateSensorList(boolean z) {
        throw new RuntimeException("Stub!");
    }

    private void removeSensorBean() {
        throw new RuntimeException("Stub!");
    }

    private InfoBean createSensor() {
        throw new RuntimeException("Stub!");
    }

    private void addSensorBean() {
        throw new RuntimeException("Stub!");
    }

    private void fillAllShowInfo(List<InfoBean> list, int i) {
        throw new RuntimeException("Stub!");
    }

    public void onLeftListVisible(boolean z) {
        throw new RuntimeException("Stub!");
    }

    private int getCardIndex(int i, List<InfoBean> list) {
        throw new RuntimeException("Stub!");
    }

    public void onLeftListIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void onLeftListSensorFault(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void onLeftCardIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void onLeftCardVisible(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void onRightListVisible(boolean z) {
        throw new RuntimeException("Stub!");
    }

    public void onRightListIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void onRightCardIndex(int i) {
        throw new RuntimeException("Stub!");
    }

    public void onRightCardVisible(boolean z) {
        throw new RuntimeException("Stub!");
    }
}