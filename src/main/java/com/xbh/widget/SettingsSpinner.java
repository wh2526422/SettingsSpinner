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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: Huan.Wang
 * @Email: huan.wang@lango-tech.cn
 * @Date: 2023/7/29 19:03
 * @Description: 此类有加动画
 */
public class SettingsSpinner extends LinearLayout {

    public static final String TAG = "SettingsSpinner";
    private int mSelectPosition;
    private PopupWindow mPopupWindow;
    private final TextView mText;
    private final ImageView mArrow;
    private RecyclerView mRecyclerView;
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

    public SettingsSpinner(Context context) {
        this(context, null);
    }

    public SettingsSpinner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SettingsSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, -1);
    }

    public SettingsSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
                itemBackGround,
                mSelectPosition
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
        mRecyclerView = new RecyclerView(this.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (listBackGround != -1) {
            mRecyclerView.setBackgroundResource(listBackGround);
        } else if (background != -1) {
            mRecyclerView.setBackgroundResource(background);
        }
        mAdapter.setOnItemClickListener((parent, view, position, id) -> {
            Log.i(TAG, "showPopWindow: " + position);
            String item = mAdapter.getItem(position);
            mText.setText(item);
            if (mSelectListener != null) {
                mSelectListener.onItemSelected(SettingsSpinner.this, view, position, id);
            }

            mPopupWindow.dismiss();
        });

        mRecyclerView.setAdapter(mAdapter);
        mPopupWindow.setContentView(mRecyclerView);

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
        mPopupWindow.showAsDropDown(this);
    }

    public interface ItemSelectListener {
        void onItemSelected(ViewGroup parent, View view, int position, long id);
    }

    private static class NiceAdapter extends RecyclerView.Adapter<SpinnerViewHolder> {
        private final List<String> dataList;
        private final int textSize;
        private final int textColor;
        private final int itemBg;
        private final int selectPosition;
        private final Context ctx;
        private ItemSelectListener mListener;

        public NiceAdapter(Context ctx, List<String> dataList, int textSize, int textColor, int itemBg, int selectPosition) {
            this.ctx = ctx;
            this.dataList = dataList;
            this.textSize = textSize;
            this.textColor = textColor;
            this.itemBg = itemBg;
            this.selectPosition = selectPosition;
        }

        public void setOnItemClickListener(ItemSelectListener listener) {
            this.mListener = listener;
        }

        @NonNull
        @Override
        public SpinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SpinnerViewHolder(LayoutInflater.from(ctx).inflate(R.layout.settings_spinner_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SpinnerViewHolder holder, int position) {

            if (textColor != -1) holder.text.setTextColor(textColor);
            if (textSize != -1) holder.text.setTextSize(textSize);
            if (itemBg != -1) holder.itemView.setBackgroundResource(itemBg);
            holder.text.setText(getItem(holder.getAdapterPosition()));

            holder.itemView.setFocusable(true);
            holder.itemView.setClickable(true);

            if (selectPosition == holder.getAdapterPosition()) {
                holder.itemView.requestFocus();
                holder.itemView.setSelected(true);
            }
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemSelected(null, v, holder.getAdapterPosition(), holder.getAdapterPosition());
                    }
                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public String getItem(int position) {
            return dataList.get(position);
        }
    }

    private static class SpinnerViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.nice_spinner_text);
        }
    }
}
