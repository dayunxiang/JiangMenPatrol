package com.zzz.ecity.android.applibrary.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Intent;
import android.location.Location;

import com.z3app.android.util.StringUtil;
import com.zzz.ecity.android.applibrary.MyApplication;
import com.zzz.ecity.android.applibrary.database.GPSPositionDao;
import com.zzz.ecity.android.applibrary.model.EPositionMessageType;
import com.zzz.ecity.android.applibrary.model.GPSPositionBean;
import com.zzz.ecity.android.applibrary.model.PositionConfig;
import com.zzz.ecity.android.applibrary.service.AReportPositionBeanBuilder;
import com.zzz.ecity.android.applibrary.service.DefaultReportPositionBeanBuilder;
import com.zzz.ecity.android.applibrary.utils.MercatorUtil;

public class PositionReportTask {
    private static PositionReportTask instance;
    private static LinkedBlockingQueue<Location> xys;
    private Location[] currentFilterData = null;
    private static boolean taskRun = false;
    private static Location lastReportLocation;
    private int continued = 0;
    private AReportPositionBeanBuilder reportPositionBeanBuilder;
    private DefaultReportPositionBeanBuilder defaultReportPositionBeanBuilder;
    private Map<String, String> filterMap = new HashMap<String, String>();

    public static PositionReportTask getInstance() {
        if (null == instance) {
            try {
                Thread.sleep(300);
                synchronized (PositionReportTask.class) {
                    if (null == instance) {
                        instance = new PositionReportTask();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private PositionReportTask() {
        xys = new LinkedBlockingQueue<Location>();
    }

    public boolean initReportPositionBeanBuilder(String className) {
        if (StringUtil.isEmpty(className)) {
            return false;
        }
        try {
            reportPositionBeanBuilder = (AReportPositionBeanBuilder) Class.forName(className).newInstance();
        } catch (Exception e) {
            sendPositionBroadcast(EPositionMessageType.BUILDPOSITIONBEANERROR, e.getMessage());
        }
        return null == reportPositionBeanBuilder ? true : false;
    }

    public synchronized void addFilterLocation(Location location) {
        if (null == location) {
            return;
        }
        if (PositionConfig.POINTS_PER_MINUTE > 60 || PositionConfig.POINTS_PER_MINUTE < 0) {
            PositionConfig.POINTS_PER_MINUTE = 10;
        }
        int timeSpacingSecond = (int) (60.f / PositionConfig.POINTS_PER_MINUTE);// 秒
        if (timeSpacingSecond < 1) {
            timeSpacingSecond = 1;
        }
        if (xys.size() > 500) {
            xys.poll();
        }
        try {
            xys.put(location);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        if (!taskRun) {
            if (null != xys && xys.size() > 0) {
                try {
                    if (null == lastReportLocation) {
                        didFilterAndReport(timeSpacingSecond);
                    } else {
                        long timeOffset = 0;
                        timeOffset = calculateTimeOffsetSecond(lastReportLocation, location);
                        if (timeOffset > timeSpacingSecond) {
                            didFilterAndReport(timeSpacingSecond);
                        }
                    }
                } catch (Exception e) {
                    sendPositionBroadcast(EPositionMessageType.BUILDPOSITIONBEANERROR, e.getMessage());
                }
            }
            taskRun = false;
        }
    }

    private synchronized void didFilterAndReport(int timeSpacingSecond) {
        if (xys != null && xys.size() > 0) {
            taskRun = true;
            int size = xys.size();
            currentFilterData = new Location[size];
            xys.toArray(currentFilterData);
            if (filterMap.size() > 300) { // 以每分钟60个坐标来估算，5分钟以前的坐标不进行筛选判断。每300个坐标点进行一次重置
                filterMap.clear();
            }
            try {
                List<Location> list = new ArrayList<Location>();
                for (int i = 0; i < currentFilterData.length; i++) {
                    list.add(currentFilterData[i]);
                }
                xys.removeAll(list);
            } catch (Exception e) {
                sendPositionBroadcast(EPositionMessageType.BUILDPOSITIONBEANERROR, e.getMessage());
            }
            if (null == currentFilterData || currentFilterData.length < 1) {
                taskRun = false;
                return;
            }
            try {
                if (1 == currentFilterData.length) {
                    if (!isSameLocation(lastReportLocation, currentFilterData[0])) {
                        try {
                            lastReportLocation = currentFilterData[0];
                            buildReportBean(currentFilterData[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Location loca = null;
                    Location selLocation = null;
                    long tf = 1;
                    for (int i = 0; i < currentFilterData.length - 1; i++) {
                        if (null == lastReportLocation) {
                            lastReportLocation = currentFilterData[i];
                            loca = currentFilterData[i + 1];
                        } else {
                            loca = currentFilterData[i];
                            if (isSameLocation(lastReportLocation, loca)) {
                                continue;
                            }
                            tf = calculateTimeOffsetSecond(lastReportLocation, loca);
                            if (tf < timeSpacingSecond) {
                                double dt = MercatorUtil.calculateLength(lastReportLocation.getLongitude(), lastReportLocation.getLatitude(), loca.getLongitude(),
                                        loca.getLatitude());
                                if (dt < PositionConfig.MAX_DISTANCE) {
                                    continue;
                                }
                            }
                            selLocation = loca;
                        }
                        if (0 == i && null == lastReportLocation) {
                            selLocation = currentFilterData[0];
                        }
                        if (null != selLocation) {
                            try {
                                buildReportBean(selLocation);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            lastReportLocation = selLocation;
                            selLocation = null;
                        }
                    }
                }
            } catch (Exception e) {
                sendPositionBroadcast(EPositionMessageType.BUILDPOSITIONBEANERROR, e.getMessage());
            }
            taskRun = false;
        } else {
            taskRun = false;
        }
    }

    // 计算两点间的时间内间隔
    private long calculateTimeOffsetSecond(Location a, Location b) {
        if (null == a || null == b)
            return 0;
        long timeoff = b.getTime() - a.getTime();
        long tf = Math.abs(timeoff / 1000);
        return tf;
    }

    private boolean isSameLocation(Location a, Location b) {
        if (null == a || null == b) {
            return false;
        }
        long timeoff = b.getTime() - a.getTime();
        if (timeoff < 1000) {
            return true;
        }
        if (Math.abs(a.getLongitude() - b.getLongitude()) < 0.000001 && Math.abs(a.getLatitude() - b.getLatitude()) < 0.000001
                && Math.abs(a.getAccuracy() - b.getAccuracy()) < 0.000001) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(b.getLongitude());
        sb.append(b.getLatitude());
        if (filterMap.containsKey(sb.toString())) {
            return true;
        }
        filterMap.put(sb.toString(), "loc");
        return false;
    }

    private void buildReportBean(Location location) {
        if (location == null || MyApplication.getApplication() == null || StringUtil.isBlank(PositionConfig.getReportUserId())) {
            return;
        }
        GPSPositionBean bean = null;
        if (null != reportPositionBeanBuilder) {
            bean = reportPositionBeanBuilder.buildGPSPositionBean(location);
        } else {
            sendPositionBroadcast(EPositionMessageType.POSITIONBEANBUILDERNOTFOUND, "");
            if (null == defaultReportPositionBeanBuilder) {
                defaultReportPositionBeanBuilder = new DefaultReportPositionBeanBuilder();
            }
            bean = defaultReportPositionBeanBuilder.buildGPSPositionBean(location);
        }
        if (null != bean) {
            savePosition(bean);
        }
    }

    /***
     * 将坐标保存到数据库
     * 
     * @param bean
     */
    private void savePosition(GPSPositionBean bean) {
        if (null == bean) {
            return;
        }
        GPSPositionBean tmpBean = new GPSPositionBean();
        tmpBean.setacu(bean.getacu());
        tmpBean.setbattery(bean.getbattery());
        tmpBean.setId(bean.getId());
        tmpBean.setlat(bean.getlat());
        tmpBean.setlon(bean.getlon());
        tmpBean.setSpeed(bean.getSpeed());
        tmpBean.setStatus(bean.getStatus());
        tmpBean.setTime(bean.getTime());
        tmpBean.setUserid(bean.getUserid());
        tmpBean.setx(bean.getx());
        tmpBean.sety(bean.gety());
        if (bean.isOverspeed()) {
            continued++;
        } else {
            continued = 0;
        }
        if (continued >= PositionConfig.MIN_OVERSPEEDPOINTS_NUM) {
            tmpBean.setOverspeed(1);
            continued = 0;
        } else {
            tmpBean.setOverspeed(0);
        }
        tmpBean.setRepay(bean.isRepay() ? 1 : 0);
        tmpBean.setNightWatch(bean.isNightWatch() ? 1 : 0);
        tmpBean.setPlanTaskId(bean.getPlanTaskId());
        tmpBean.setInDetourArea(bean.isInDetourArea() ? 1 : 0);
        sendPositionBroadcast(EPositionMessageType.FILTERNEWLOCATION, "");
        try {
            GPSPositionDao.getInstance().savePositionBean(tmpBean);
        } catch (Exception e) {
            e.printStackTrace();
            sendPositionBroadcast(EPositionMessageType.POSITIONBEANSAVEFAIL, e.getMessage());
        }
    }

    private void sendPositionBroadcast(EPositionMessageType type, String msg) {
        Intent intent = new Intent(PositionConfig.ACTION_POSITION_NAME);
        intent.putExtra(PositionConfig.ACTION_POSITION_MSG_TYPE, type.getValue());
        intent.putExtra(PositionConfig.ACTION_POSITION_MSG_CONTENT, msg);
        MyApplication.getApplication().sendBroadcast(intent);
    }
}
