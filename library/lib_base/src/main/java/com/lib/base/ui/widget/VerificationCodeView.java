package com.lib.base.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.lib.base.R;

/**
 * 分格验证码：底层单个 {@link EditText} 承接输入/粘贴/短信自动填充，上层格子只做展示。
 * <ul>
 *   <li>{@link #setCode(CharSequence)} 程序赋值（会按 inputType 过滤）</li>
 *   <li>系统粘贴、短信 OTP autofill 走同一路径</li>
 *   <li>不强制关软键盘，通用登录页可直接用</li>
 * </ul>
 */
public class VerificationCodeView extends FrameLayout {

    public enum InputTypeMode {
        NUMBER,
        NUMBER_PASSWORD,
        TEXT,
        TEXT_PASSWORD
    }

    public interface OnCodeChangeListener {
        void onTextChange(@NonNull VerificationCodeView view, @NonNull String code);

        void onComplete(@NonNull VerificationCodeView view, @NonNull String code);
    }

    private final LinearLayout cellContainer;
    private final EditText input;
    private final TextView[] cells;

    private int cellCount = 6;
    private InputTypeMode inputTypeMode = InputTypeMode.NUMBER;
    private int cellWidthPx;
    private int cellHeightPx;
    private int textColor = Color.BLACK;
    private float textSizePx;
    private int cellBgRes;
    private int cellBgFilledRes;
    private int cellSpacingPx;
    private boolean bisect = true;
    private String passwordChar = "•";

    @Nullable
    private OnCodeChangeListener listener;
    private boolean updatingUi;

    public VerificationCodeView(@NonNull Context context) {
        this(context, null);
    }

    public VerificationCodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerificationCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        cellWidthPx = Math.round(48f * density);
        cellHeightPx = cellWidthPx;
        textSizePx = 18f * density;
        cellSpacingPx = Math.round(8f * density);
        cellBgRes = R.drawable.bg_vcv_cell;
        cellBgFilledRes = R.drawable.bg_vcv_cell_filled;

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerificationCodeView);
            cellCount = Math.max(1, ta.getInteger(R.styleable.VerificationCodeView_vcv_et_number, cellCount));
            int typeOrd = ta.getInt(R.styleable.VerificationCodeView_vcv_et_inputType, 0);
            InputTypeMode[] modes = InputTypeMode.values();
            inputTypeMode = modes[Math.max(0, Math.min(typeOrd, modes.length - 1))];
            cellWidthPx = ta.getDimensionPixelSize(R.styleable.VerificationCodeView_vcv_et_width, cellWidthPx);
            cellHeightPx = ta.getDimensionPixelSize(R.styleable.VerificationCodeView_vcv_et_height, cellWidthPx);
            textColor = ta.getColor(R.styleable.VerificationCodeView_vcv_et_text_color, textColor);
            textSizePx = ta.getDimension(R.styleable.VerificationCodeView_vcv_et_text_size, textSizePx);
            cellBgRes = ta.getResourceId(R.styleable.VerificationCodeView_vcv_et_bg, cellBgRes);
            cellBgFilledRes = ta.getResourceId(R.styleable.VerificationCodeView_vcv_et_bg_filled, cellBgFilledRes);
            cellSpacingPx = ta.getDimensionPixelSize(R.styleable.VerificationCodeView_vcv_et_spacing, cellSpacingPx);
            bisect = ta.getBoolean(R.styleable.VerificationCodeView_vcv_et_bisect, true);
            String pwd = ta.getString(R.styleable.VerificationCodeView_vcv_et_password_char);
            if (!TextUtils.isEmpty(pwd)) {
                passwordChar = pwd.substring(0, 1);
            }
            ta.recycle();
        }

        cellContainer = new LinearLayout(context);
        cellContainer.setOrientation(LinearLayout.HORIZONTAL);
        cellContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(cellContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        cells = new TextView[cellCount];
        for (int i = 0; i < cellCount; i++) {
            TextView cell = new AppCompatTextView(context);
            cell.setGravity(Gravity.CENTER);
            cell.setIncludeFontPadding(false);
            cell.setTextColor(textColor);
            cell.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
            cell.setBackgroundResource(cellBgRes);
            cell.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            cells[i] = cell;
            cellContainer.addView(cell);
        }

        input = new AppCompatEditText(context);
        input.setBackground(null);
        input.setCursorVisible(true);
        input.setTextColor(Color.TRANSPARENT);
        input.setHintTextColor(Color.TRANSPARENT);
        input.setHighlightColor(Color.TRANSPARENT);
        input.setGravity(Gravity.CENTER);
        input.setPadding(0, 0, 0, 0);
        input.setMaxLines(1);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setLongClickable(true);
        input.setTextIsSelectable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            input.setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_YES);
            // 短信 OTP；常量 API 28+，字符串兼容 26
            input.setAutofillHints("smsOTP");
        }
        applyInputType();
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(cellCount), this::filterChar});
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (updatingUi) {
                    return;
                }
                String filtered = filterCode(s == null ? "" : s.toString());
                if (!TextUtils.equals(filtered, s == null ? "" : s.toString())) {
                    updatingUi = true;
                    input.setText(filtered);
                    input.setSelection(filtered.length());
                    updatingUi = false;
                }
                refreshCells(filtered);
                notifyChanged(filtered);
            }
        });
        addView(input, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        setOnClickListener(v -> requestInputFocus());
        refreshCells("");
    }

    private CharSequence filterChar(CharSequence source, int start, int end,
                                    android.text.Spanned dest, int dstart, int dend) {
        if (source == null || start >= end) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (acceptChar(c)) {
                sb.append(c);
            }
        }
        return sb.length() == (end - start) ? null : sb;
    }

    private boolean acceptChar(char c) {
        switch (inputTypeMode) {
            case NUMBER:
            case NUMBER_PASSWORD:
                return Character.isDigit(c);
            case TEXT:
            case TEXT_PASSWORD:
            default:
                return !Character.isWhitespace(c);
        }
    }

    private String filterCode(@Nullable CharSequence raw) {
        if (raw == null || raw.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(cellCount);
        for (int i = 0; i < raw.length() && sb.length() < cellCount; i++) {
            char c = raw.charAt(i);
            if (acceptChar(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private void applyInputType() {
        switch (inputTypeMode) {
            case NUMBER_PASSWORD:
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                break;
            case TEXT:
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                break;
            case TEXT_PASSWORD:
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case NUMBER:
            default:
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
        }
    }

    private void refreshCells(@NonNull String code) {
        boolean password = inputTypeMode == InputTypeMode.NUMBER_PASSWORD
                || inputTypeMode == InputTypeMode.TEXT_PASSWORD;
        int focusIndex = Math.min(code.length(), cellCount - 1);
        for (int i = 0; i < cellCount; i++) {
            TextView cell = cells[i];
            if (i < code.length()) {
                cell.setText(password ? passwordChar : String.valueOf(code.charAt(i)));
                cell.setBackgroundResource(cellBgFilledRes != 0 ? cellBgFilledRes : cellBgRes);
                cell.setSelected(false);
            } else {
                cell.setText("");
                cell.setBackgroundResource(cellBgRes);
                cell.setSelected(i == focusIndex && code.length() < cellCount);
            }
        }
    }

    private void notifyChanged(@NonNull String code) {
        if (listener == null) {
            return;
        }
        listener.onTextChange(this, code);
        if (code.length() >= cellCount) {
            listener.onComplete(this, code);
        }
    }

    private void layoutCells(int width) {
        if (width <= 0 || cellCount <= 0) {
            return;
        }
        int pad = getPaddingStart() + getPaddingEnd();
        int contentW = Math.max(0, width - pad);
        int boxW;
        int gap;
        if (bisect) {
            // 等分：格子等宽，间距均分剩余
            int totalGap = Math.max(0, cellSpacingPx * (cellCount - 1));
            boxW = Math.max(1, (contentW - totalGap) / cellCount);
            gap = cellSpacingPx;
            // 若未显式给足够 spacing，仍保证铺满：重算 gap
            if (boxW * cellCount + gap * (cellCount - 1) < contentW) {
                int remain = contentW - boxW * cellCount;
                gap = cellCount > 1 ? remain / (cellCount - 1) : 0;
            }
        } else {
            boxW = cellWidthPx;
            gap = cellSpacingPx;
        }
        int boxH = cellHeightPx > 0 ? cellHeightPx : boxW;
        for (int i = 0; i < cellCount; i++) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cells[i].getLayoutParams();
            if (lp == null) {
                lp = new LinearLayout.LayoutParams(boxW, boxH);
            } else {
                lp.width = boxW;
                lp.height = boxH;
            }
            lp.setMarginStart(i == 0 ? 0 : gap);
            lp.setMarginEnd(0);
            cells[i].setLayoutParams(lp);
        }
        LayoutParams inputLp = (LayoutParams) input.getLayoutParams();
        inputLp.height = boxH;
        input.setLayoutParams(inputLp);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        layoutCells(width);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnCodeChangeListener(@Nullable OnCodeChangeListener listener) {
        this.listener = listener;
    }

    /** 当前已输入内容（未满格也可能有值）。 */
    @NonNull
    public String getCode() {
        Editable e = input.getText();
        return e == null ? "" : e.toString();
    }

    /**
     * 程序赋值（粘贴同路径）：自动按类型过滤并截断到格数。
     */
    public void setCode(@Nullable CharSequence code) {
        String filtered = filterCode(code);
        updatingUi = true;
        input.setText(filtered);
        input.setSelection(filtered.length());
        updatingUi = false;
        refreshCells(filtered);
        notifyChanged(filtered);
    }

    public void clearCode() {
        setCode("");
        requestInputFocus();
    }

    public void requestInputFocus() {
        input.requestFocus();
        input.setSelection(getCode().length());
    }

    public EditText getInput() {
        return input;
    }

    public int getCellCount() {
        return cellCount;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        input.setEnabled(enabled);
        for (TextView cell : cells) {
            cell.setEnabled(enabled);
            cell.setAlpha(enabled ? 1f : 0.45f);
        }
    }
}
