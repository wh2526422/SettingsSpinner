package com.xbh.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: Huan.Wang
 * @Email: huan.wang@lango-tech.cn
 * @Date: 2023/7/29 19:03
 * @Description: 此类未有加动画
 */
public class SettingsSpinner_bak extends LinearLayout {

    public static final String TAG = "SettingsSpinner";
    private int mSelectPosition;
    private PopupWindow mPopupWindow;
    private final TextView mText;
    private final ImageView mArrow;
    private ListView mListView;
    private int background = 0;
    private int listBackGround = 0;
    private int itemBackGround = 0;
    private int itemTextColor = 0;
    private int itemTextSize = 0;
    private int mPopWindowHeight = 0;
    private final int mTextColor;
    private final int mArrowColor;
    private ItemSelectListener mSelectListener;
    private NiceAdapter mAdapter;
    private List<String> mDatas;

    public SettingsSpinner_bak(Context context) {
        this(context, null);
    }

    public SettingsSpinner_bak(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SettingsSpinner_bak(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    public SettingsSpinner_bak(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Resources resources = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsSpinner);
        Resources.Theme theme = context.getTheme();

        mTextColor = typedArray.getColor(R.styleable.SettingsSpinner_android_textColor, -1);
        int entries = typedArray.getResourceId(R.styleable.SettingsSpinner_android_entries, -1);
        mArrowColor = typedArray.getColor(R.styleable.SettingsSpinner_arrowColor, -1);
        listBackGround = typedArray.getResourceId(R.styleable.SettingsSpinner_listBackground, -1);
        itemBackGround = typedArray.getResourceId(R.styleable.SettingsSpinner_itemBackground, -1);
        itemTextColor = typedArray.getColor(R.styleable.SettingsSpinner_itemTextColor, -1);
        itemTextSize = typedArray.getDimensionPixelSize(R.styleable.SettingsSpinner_itemTextSize, -1);
        background = typedArray.getResourceId(R.styleable.SettingsSpinner_android_background, android.R.drawable.btn_default_small);
        mPopWindowHeight = typedArray.getDimensionPixelSize(R.styleable.SettingsSpinner_popupWindowHeight, -1);
        int arrowDrawable = typedArray.getResourceId(R.styleable.SettingsSpinner_arrowDrawable, R.drawable.arrow);
        float textSize = typedArray.getDimension(R.styleable.SettingsSpinner_android_textSize, -1);
        boolean enabled = typedArray.getBoolean(R.styleable.SettingsSpinner_android_enabled, true);

        int padding = getResources().getDimensionPixelSize(R.dimen.spinner_padding);

        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.VERTICAL_GRAVITY_MASK);
        this.setPadding(padding, padding, padding, padding);

        setBackgroundResource(background);
        setEnabled(enabled);

        if (entries != -1) {
            String[] dataArray = resources.getStringArray(entries);
            mDatas = Arrays.asList(dataArray);
        }

        mText = new TextView(context);
        LayoutParams textParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.weight = 1;
        mText.setLayoutParams(textParams);

        if (!enabled) {
            mText.setTextColor(Color.GRAY);
        } else if (mTextColor != -1) {
            mText.setTextColor(mTextColor);
        }

        if (textSize != -1) {
            mText.setTextSize(textSize);
        }
        if (mDatas != null) {
            mText.setText(mDatas.get(0));
        }
        addView(mText);

        mArrow = new ImageView(context);
        mArrow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable drawable = resources.getDrawable(arrowDrawable, theme);

        if (!enabled) {
            drawable.setTint(Color.GRAY);
        } else if (mArrowColor != -1) {
            drawable.setTint(mArrowColor);
        }
        mArrow.setBackground(drawable);
        addView(mArrow);

        typedArray.recycle();

        this.setClickable(true);
        this.setFocusable(true);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator =
                        ObjectAnimator.ofFloat(mArrow, "rotation", 0, 180.0f);
                mArrow.setPivotX(mArrow.getWidth() >> 1);
                mArrow.setPivotY(mArrow.getHeight() >> 1);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration(200);
                animator.start();
                showPopWindow();
            }
        });

        if (mDatas != null) {
            initAdapter();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mArrow == null || mText == null) return;
        Drawable arrowBackground = mArrow.getBackground();
        if (!enabled) {
            arrowBackground.setTint(Color.GRAY);
            mArrow.setBackground(arrowBackground);
            mText.setTextColor(Color.GRAY);
        } else {
            arrowBackground.setTint(mArrowColor == -1 ? Color.WHITE : mArrowColor);
            mArrow.setBackground(arrowBackground);
            mText.setTextColor(mTextColor == -1 ? Color.WHITE : mTextColor);
        }
    }

    public void attachDataSource(List<String> datas) {
        this.mDatas = datas;
        mAdapter = null;
        initAdapter();
    }

    private void initAdapter() {
        mAdapter = new NiceAdapter(
                getContext(),
                mDatas,
                itemTextSize,
                itemTextColor,
                itemBackGround
        );
    }

    public void setOnItemSelectListener(ItemSelectListener listener) {
        this.mSelectListener = listener;
    }

    public void setSelectItem(int position) {
        mSelectPosition = position;
        if (mAdapter != null) {
            String item = mAdapter.getItem(position);
            mText.setText(item);
        }
    }

    public void setSelectItemTitle(String item) {
        mText.setText(item);
    }

    private void showPopWindow() {
        if (mDatas == null || mAdapter == null) {
            Log.e(TAG, "showPopWindow: data is null");
            return;
        }
        mPopupWindow = new PopupWindow();
        mListView = new ListView(this.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mListView.setLayoutParams(params);

        if (listBackGround != -1) {
            mListView.setBackgroundResource(listBackGround);
        } else if (background != -1) {
            mListView.setBackgroundResource(background);
        }
        mAdapter.setOnItemClickListener((parent, view, position, id) -> {
            String item = mAdapter.getItem(position);
            mText.setText(item);
            if (mSelectListener != null) {
                mSelectListener.onItemSelected(SettingsSpinner_bak.this, view, position, id);
            }

            mPopupWindow.dismiss();
        });
        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);


        if (mPopWindowHeight != -1) {
            mPopupWindow.setHeight(mPopWindowHeight);
        } else {
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mArrow, "rotation", 180.0f, 0);
                mArrow.setPivotX(mArrow.getWidth() >> 1);
                mArrow.setPivotY(mArrow.getHeight() >> 1);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.setDuration(200);
                animator.start();
            }
        });
        mPopupWindow.setWidth(getWidth());
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
    }

    public interface ItemSelectListener {
        void onItemSelected(ViewGroup parent, View view, int position, long id);
    }

    private static class NiceAdapter extends BaseAdapter {
        private final List<String> dataList;
        private final int textSize;
        private final int textColor;
        private final int itemBg;
        private final Context ctx;
        private ItemSelectListener mListener;

        public NiceAdapter(Context ctx, List<String> dataList, int textSize, int textColor, int itemBg) {
            this.ctx = ctx;
            this.dataList = dataList;
            this.textSize = textSize;
            this.textColor = textColor;
            this.itemBg = itemBg;
        }

        public void setOnItemClickListener(ItemSelectListener listener) {
            this.mListener = listener;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public String getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(ctx).inflate(R.layout.settings_spinner_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (textColor != -1) holder.text.setTextColor(textColor);
            if (textSize != -1) holder.text.setTextSize(textSize);
            if (itemBg != -1) holder.root.setBackgroundResource(itemBg);
            holder.root.setFocusable(true);
            holder.root.setClickable(true);
            holder.text.setText(dataList.get(position));

            holder.root.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemSelected(null, v, position, position);
                    }
                }
            });
            return holder.root;
        }
    }

    private static class ViewHolder {
        View root;
        TextView text;

        public ViewHolder(View root) {
            this.root = root;
            text = root.findViewById(R.id.nice_spinner_text);
        }
    }

}
