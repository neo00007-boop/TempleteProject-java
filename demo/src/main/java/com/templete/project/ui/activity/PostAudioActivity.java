package com.templete.project.ui.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lib.base.ui.activity.BaseActivity;
import com.templete.project.R;
import com.templete.project.databinding.ActivityPostAudioBinding;
import com.lib.base.ui.widget.audio.AudioPlayManager;
import com.lib.base.ui.widget.audio.PostAudioView;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子音频组件 Demo：确定性波形 + 多实例互斥播放。
 */
public class PostAudioActivity extends BaseActivity<ActivityPostAudioBinding> {

    @Override
    protected ActivityPostAudioBinding viewBinding() {
        return ActivityPostAudioBinding.inflate(getLayoutInflater());
    }

    @Override
    public void inits() {
        setTitleStr("帖子音频组件");
    }

    @Override
    public void initView() {
        mViewBinding.tvHint.setText("波形由 url+固定盐确定性生成（不下载音频）；同时只能播放一条");
        // 顶部自定义属性样式示例
        mViewBinding.postAudioCustom.setAudioUrl(
                "https://loremfile.dev/mp3/sine-440hz-3s.mp3");
        mViewBinding.rvAudioList.setLayoutManager(new LinearLayoutManager(this));
        mViewBinding.rvAudioList.setAdapter(new AudioAdapter(buildDemoList()));
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void initData() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        AudioPlayManager.get().pause();
    }

    @Override
    public void onDestroy() {
        mViewBinding.postAudioCustom.release();
        AudioPlayManager.get().releaseAll();
        super.onDestroy();
    }

    private List<AudioItem> buildDemoList() {
        // 下列均为可直接热链的开源/测试 MP3（已 HEAD 校验 200 + audio/mpeg）
        List<AudioItem> list = new ArrayList<>();
        list.add(new AudioItem("短音 3s · loremfile (CC0)",
                "https://loremfile.dev/mp3/sine-440hz-3s.mp3"));
        list.add(new AudioItem("立体声 5s · loremfile (CC0)",
                "https://loremfile.dev/mp3/stereo-lr-5s.mp3"));
        list.add(new AudioItem("中长 30s · loremfile (CC0)",
                "https://loremfile.dev/mp3/sine-440hz-30s.mp3"));
        list.add(new AudioItem("约 6s · TrueFileSize 100KB (CC0)",
                "https://cdn.truefilesize.com/mp3/sample-100kb.mp3"));
        list.add(new AudioItem("约 30s · TrueFileSize 500KB (CC0)",
                "https://cdn.truefilesize.com/mp3/sample-500kb.mp3"));
        list.add(new AudioItem("约 1min · TrueFileSize 1MB (CC0)",
                "https://cdn.truefilesize.com/mp3/sample-1mb.mp3"));
        list.add(new AudioItem("FileSamples sample1",
                "https://filesamples.com/samples/audio/mp3/sample1.mp3"));
        list.add(new AudioItem("FileSamples sample2",
                "https://filesamples.com/samples/audio/mp3/sample2.mp3"));
        list.add(new AudioItem("FileSamples sample3",
                "https://filesamples.com/samples/audio/mp3/sample3.mp3"));
        list.add(new AudioItem("Kalimba · learningcontainer",
                "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3"));
        list.add(new AudioItem("SampleLib 15s",
                "https://download.samplelib.com/mp3/sample-15s.mp3"));
        list.add(new AudioItem("SoundHelix Song 1（长曲）",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        list.add(new AudioItem("SoundHelix Song 2（长曲）",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"));
        list.add(new AudioItem("同一 url 波形应一致 · 再绑 Song1",
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"));
        list.add(new AudioItem("Archive.org 测试短音频",
                "https://archive.org/download/testmp3testfile/mpthreetest.mp3"));
        return list;
    }

    private static final class AudioItem {
        final String title;
        final String url;

        AudioItem(String title, String url) {
            this.title = title;
            this.url = url;
        }
    }

    private static final class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.Holder> {
        private final List<AudioItem> data;

        AudioAdapter(List<AudioItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post_audio, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            AudioItem item = data.get(position);
            holder.tvTitle.setText(item.title);
            holder.postAudioView.setAudioUrl(item.url);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onViewRecycled(@NonNull Holder holder) {
            holder.postAudioView.release();
            super.onViewRecycled(holder);
        }

        static final class Holder extends RecyclerView.ViewHolder {
            final TextView tvTitle;
            final PostAudioView postAudioView;

            Holder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                postAudioView = itemView.findViewById(R.id.postAudioView);
            }
        }
    }
}
