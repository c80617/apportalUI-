//package brahma.vmi.brahmalibrary.apprtc;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Point;
//import android.util.AttributeSet;
//import android.view.SurfaceHolder;
//
//import org.webrtc.EglBase;
//import org.webrtc.EglRenderer;
//import org.webrtc.GlRectDrawer;
//import org.webrtc.Logging;
//import org.webrtc.RendererCommon;
//import org.webrtc.SurfaceViewRenderer;
//import org.webrtc.ThreadUtils;
//import org.webrtc.VideoFrame;
//import org.webrtc.VideoRenderer;
//import org.webrtc.VideoRenderer.I420Frame;
//import org.webrtc.VideoSink;
//
//import java.util.concurrent.CountDownLatch;
//import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.widthPixels;
//import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.heightPixels;
//
///**
// * @file MySurfaceViewRender
// * @brief Override WebRTC的SurfaceViewRenderer
// *
// * 重新定義WebRTC的SurfaceViewRenderer,
// * 主要目的為保證顯示的螢幕為滿版畫面
// * 避免畫面自行調整比例造成畫面的黑邊
// * @author YiWen Li
// * @date 2019/07/12
// **/
//
//public class MySurfaceViewRender extends SurfaceViewRenderer implements SurfaceHolder.Callback, VideoRenderer.Callbacks, VideoSink {
//    private static final String TAG = "MySurfaceViewRender";
//    private final String resourceName = this.getResourceName();
//    private final RendererCommon.VideoLayoutMeasure videoLayoutMeasure = new RendererCommon.VideoLayoutMeasure();
//    private final EglRenderer eglRenderer;
//    private RendererCommon.RendererEvents rendererEvents;
//    private final Object layoutLock = new Object();
//    private boolean isRenderingPaused = false;
//    private boolean isFirstFrameRendered;
//    private int rotatedFrameWidth;
//    private int rotatedFrameHeight;
//    private int frameRotation;
//    private boolean enableFixedSize;
//    private int surfaceWidth;
//    private int surfaceHeight;
//
//    public MySurfaceViewRender(Context context) {
//        super(context);
//        this.eglRenderer = new EglRenderer(this.resourceName);
//        this.getHolder().addCallback(this);
//    }
//
//    public MySurfaceViewRender(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.eglRenderer = new EglRenderer(this.resourceName);
//        this.getHolder().addCallback(this);
//    }
//
//    public void init(org.webrtc.EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents) {
//        this.init(sharedContext, rendererEvents, EglBase.CONFIG_PLAIN, new GlRectDrawer());
//    }
//
//    public void init(org.webrtc.EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents, int[] configAttributes, RendererCommon.GlDrawer drawer) {
//        ThreadUtils.checkIsOnMainThread();
//        this.rendererEvents = rendererEvents;
//        Object var5 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            this.isFirstFrameRendered = false;
//            this.rotatedFrameWidth = 0;
//            this.rotatedFrameHeight = 0;
//            this.frameRotation = 0;
//        }
//
//        this.eglRenderer.init(sharedContext, configAttributes, drawer);
//    }
//
//    public void release() {
//        this.eglRenderer.release();
//    }
//
//    public void addFrameListener(EglRenderer.FrameListener listener, float scale, RendererCommon.GlDrawer drawerParam) {
//        this.eglRenderer.addFrameListener(listener, scale, drawerParam);
//    }
//
//    public void addFrameListener(EglRenderer.FrameListener listener, float scale) {
//        this.eglRenderer.addFrameListener(listener, scale);
//    }
//
//    public void removeFrameListener(EglRenderer.FrameListener listener) {
//        this.eglRenderer.removeFrameListener(listener);
//    }
//
//    public void setEnableHardwareScaler(boolean enabled) {
//        ThreadUtils.checkIsOnMainThread();
//        this.enableFixedSize = enabled;
//        this.updateSurfaceSize();
//    }
//
//    public void setMirror(boolean mirror) {
//        this.eglRenderer.setMirror(mirror);
//    }
//
//    public void setScalingType(RendererCommon.ScalingType scalingType) {
//        ThreadUtils.checkIsOnMainThread();
//        this.videoLayoutMeasure.setScalingType(scalingType);
//        this.requestLayout();
//    }
//
//    public void setScalingType(RendererCommon.ScalingType scalingTypeMatchOrientation, RendererCommon.ScalingType scalingTypeMismatchOrientation) {
//        ThreadUtils.checkIsOnMainThread();
//        this.videoLayoutMeasure.setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation);
//        this.requestLayout();
//    }
//
//    public void setFpsReduction(float fps) {
//        Object var2 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            this.isRenderingPaused = fps == 0.0F;
//        }
//
//        this.eglRenderer.setFpsReduction(fps);
//    }
//
//    public void disableFpsReduction() {
//        Object var1 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            this.isRenderingPaused = false;
//        }
//
//        this.eglRenderer.disableFpsReduction();
//    }
//
//    public void pauseVideo() {
//        Object var1 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            this.isRenderingPaused = true;
//        }
//
//        this.eglRenderer.pauseVideo();
//    }
//
//    public void renderFrame(I420Frame frame) {
//        this.updateFrameDimensionsAndReportEvents(frame);
//        //this.getHolder().setFixedSize(1440, 2560);
//        this.eglRenderer.renderFrame(frame);
//    }
//
//    public void onFrame(VideoFrame frame) {
//        this.updateFrameDimensionsAndReportEvents(frame);
//        this.eglRenderer.onFrame(frame);
//    }
//
//    protected void onMeasure(int widthSpec, int heightSpec) {
//        ThreadUtils.checkIsOnMainThread();
//        Object var4 = this.layoutLock;
//        Point size;
//        synchronized(this.layoutLock) {
//            size = this.videoLayoutMeasure.measure(widthSpec, heightSpec, this.rotatedFrameWidth, this.rotatedFrameHeight);
//        }
////        Log.d(TAG,"widthSpec：" + widthSpec+" ,heightSpec:" + heightSpec);
////        Log.d(TAG,"rotatedFrameWidth：" + this.rotatedFrameWidth+" ,rotatedFrameHeight:" + this.rotatedFrameHeight);
//        this.setMeasuredDimension(size.x, size.y);
////        this.logD("onMeasure(). New size: " + size.x + "x" + size.y);
//    }
//
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        ThreadUtils.checkIsOnMainThread();
//        this.eglRenderer.setLayoutAspectRatio((float)(right - left) / (float)(bottom - top));
//        this.updateSurfaceSize();
//    }
//
//    private void updateSurfaceSize() {
////        Log.d(TAG,"updateSurfaceSize");
////        Log.d(TAG,"rotatedFrameWidth:"+rotatedFrameWidth);
////        Log.d(TAG,"rotatedFrameHeight:"+rotatedFrameHeight);
////        Log.d(TAG,"this.getWidth():"+this.getWidth());
////        Log.d(TAG,"this.getHeight():"+this.getHeight());
//        ThreadUtils.checkIsOnMainThread();
//        Object var1 = this.layoutLock;
//        synchronized(this.layoutLock) {
//
//            if (this.enableFixedSize && this.rotatedFrameWidth != 0 && this.rotatedFrameHeight != 0 && this.getWidth() != 0 && this.getHeight() != 0) {
//                float layoutAspectRatio = (float)this.getWidth() / (float)this.getHeight();
//                float frameAspectRatio = (float)this.rotatedFrameWidth / (float)this.rotatedFrameHeight;
//                int drawnFrameWidth;
//                int drawnFrameHeight;
//                if (frameAspectRatio > layoutAspectRatio) {
//                    drawnFrameWidth = (int)((float)this.rotatedFrameHeight * layoutAspectRatio);
//                    drawnFrameHeight = this.rotatedFrameHeight;
//                } else {
//                    drawnFrameWidth = this.rotatedFrameWidth;
//                    drawnFrameHeight = (int)((float)this.rotatedFrameWidth / layoutAspectRatio);
//                }
//
//                int width = Math.min(this.getWidth(), drawnFrameWidth);
//                int height = Math.min(this.getHeight(), drawnFrameHeight);
////                this.logD("updateSurfaceSize. Layout size: " + this.getWidth() + "x" + this.getHeight() + ", frame size: " + this.rotatedFrameWidth + "x" + this.rotatedFrameHeight + ", requested surface size: " + width + "x" + height + ", old surface size: " + this.surfaceWidth + "x" + this.surfaceHeight);
//                if (width != this.surfaceWidth || height != this.surfaceHeight) {
//                    this.surfaceWidth = width;
//                    this.surfaceHeight = height;
//                    this.getHolder().setFixedSize(width, height);
//                }
//            } else {
//                this.surfaceWidth = this.surfaceHeight = 0;
//                //this.getHolder().setSizeFromLayout();
//
//            }
//            this.getHolder().setFixedSize(widthPixels, heightPixels);
//        }
//    }
//
//    public void surfaceCreated(SurfaceHolder holder) {
//        ThreadUtils.checkIsOnMainThread();
//        this.eglRenderer.createEglSurface(holder.getSurface());
//        this.surfaceWidth = this.surfaceHeight = 0;
//        this.updateSurfaceSize();
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        ThreadUtils.checkIsOnMainThread();
//        final CountDownLatch completionLatch = new CountDownLatch(1);
//        this.eglRenderer.releaseEglSurface(new Runnable() {
//            public void run() {
//                completionLatch.countDown();
//            }
//        });
//        ThreadUtils.awaitUninterruptibly(completionLatch);
//    }
//
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        ThreadUtils.checkIsOnMainThread();
//        this.logD("surfaceChanged: format: " + format + " size: " + width + "x" + height);
//    }
//
//    private String getResourceName() {
//        try {
//            return this.getResources().getResourceEntryName(this.getId()) + ": ";
//        } catch (Resources.NotFoundException var2) {
//            return "";
//        }
//    }
//
//    public void clearImage() {
//        this.eglRenderer.clearImage();
//    }
//
//    private void updateFrameDimensionsAndReportEvents(I420Frame frame) {
////        Log.d(TAG,"frame.width:"+frame.width);
////        Log.d(TAG,"frame.height:"+frame.height);
////        Log.d(TAG,"frame.rotationDegree:"+frame.rotationDegree);
//        Object var2 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            if (!this.isRenderingPaused) {
//                if (!this.isFirstFrameRendered) {
//                    this.isFirstFrameRendered = true;
//                    this.logD("Reporting first rendered frame.");
//                    if (this.rendererEvents != null) {
//                        this.rendererEvents.onFirstFrameRendered();
//                    }
//                }
//
//                if (this.rotatedFrameWidth != frame.rotatedWidth() || this.rotatedFrameHeight != frame.rotatedHeight() || this.frameRotation != frame.rotationDegree) {
//                    this.logD("Reporting frame resolution changed to " + frame.width + "x" + frame.height + " with rotation " + frame.rotationDegree);
//                    if (this.rendererEvents != null) {
//                        this.rendererEvents.onFrameResolutionChanged(frame.width, frame.height, frame.rotationDegree);
//                    }
//
//                    this.rotatedFrameWidth = frame.rotatedWidth();
//                    this.rotatedFrameHeight = frame.rotatedHeight();
//                    this.frameRotation = frame.rotationDegree;
//                    this.post(new Runnable() {
//                        public void run() {
//                            MySurfaceViewRender.this.updateSurfaceSize();
//                            MySurfaceViewRender.this.requestLayout();
//                        }
//                    });
//                }
//            }else{
//            }
//        }
//    }
//
//    private void updateFrameDimensionsAndReportEvents(VideoFrame frame) {
//        Object var2 = this.layoutLock;
//        synchronized(this.layoutLock) {
//            if (!this.isRenderingPaused) {
//                if (!this.isFirstFrameRendered) {
//                    this.isFirstFrameRendered = true;
//                    this.logD("Reporting first rendered frame.");
//                    if (this.rendererEvents != null) {
//                        this.rendererEvents.onFirstFrameRendered();
//                    }
//                }
//
//                if (this.rotatedFrameWidth != frame.getRotatedWidth() || this.rotatedFrameHeight != frame.getRotatedHeight() || this.frameRotation != frame.getRotation()) {
//                    this.logD("Reporting frame resolution changed to " + frame.getBuffer().getWidth() + "x" + frame.getBuffer().getHeight() + " with rotation " + frame.getRotation());
//                    if (this.rendererEvents != null) {
//                        this.rendererEvents.onFrameResolutionChanged(frame.getBuffer().getWidth(), frame.getBuffer().getHeight(), frame.getRotation());
//                    }
//
//                    this.rotatedFrameWidth = frame.getRotatedWidth();
//                    this.rotatedFrameHeight = frame.getRotatedHeight();
//                    this.frameRotation = frame.getRotation();
//                    //this.post(new SurfaceViewRenderer$$Lambda$0(this));
//                }
//
//            }
//        }
//    }
//
//    private void logD(String string) {
//        Logging.d(TAG, this.resourceName + string);
//    }
//}
