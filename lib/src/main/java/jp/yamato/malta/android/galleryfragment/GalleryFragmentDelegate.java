package jp.yamato.malta.android.galleryfragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by malta on 2017/05/02.
 * GalleryFragment
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class GalleryFragmentDelegate {
    @SuppressWarnings("unused")
    private static final String TAG = "GalleryFragmentDelegate";

    private static final String ARG_TOP_RESOURCE = "arg_top_resource";
    private static final String ARG_RESOURCE = "arg_resource";
    private static final String ARG_EMPTY_RESOURCE = "arg_empty_resource";
    private static final String ARG_MAX_TASK_COUNT = "arg_max_task_count";
    private static final String ARG_LAYOUT = "arg_layout";
    private static final String ARG_SPAN_COUNT = "arg_span_count";
    private static final String ARG_DATA = "arg_data";

    private static final String SAVE_TOP_RESOURCE = "save_top_resource";
    private static final String SAVE_RESOURCE = "save_resource";
    private static final String SAVE_EMPTY_RESOURCE = "save_empty_resource";
    private static final String SAVE_MAX_TASK_COUNT = "save_max_task_count";
    private static final String SAVE_LAYOUT = "save_layout";
    private static final String SAVE_SPAN_COUNT = "save_span_count";
    private static final String SAVE_DATA = "save_data";

    private Context mContext;

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private ImageAdapter.LoadTask.BitmapLoader mBitmapLoader;
    private ImageAdapter.OnItemClickListener mOnItemClickListener;
    private FormatterPickable mFormatterPickable;

    // state
    private int mTopResource = R.layout.jp_yamato_malta_gallery_fragment_simple_recyclerview;
    private int mResource = R.layout.jp_yamato_malta_gallery_fragment_simple_selectable_image_item;
    private int mEmptyResource = android.R.drawable.alert_light_frame;
    private int mMaxTaskCount = ImageAdapter.LoadTask.MAX_TASK_COUNT;
    private int mLayout = GalleryFragmentParams.GRID_LAYOUT;
    private int mSpanCount = 2;

    private boolean mIsTopResourceFieldAvailable = false;
    private boolean mIsResourceFieldAvailable = false;
    private boolean mIsEmptyResourceFieldAvailable = false;
    private boolean mIsMaxTaskCountAvailable = false;
    private boolean mIsLayoutFieldAvailable = false;
    private boolean mIsSpanCountFieldAvailable = false;

    private DeferredOperations mDeferredOperations;

    public GalleryFragmentDelegate() {
        mDeferredOperations = new DeferredOperations();
    }

    public static void setArguments(Fragment instance, int resource) {
        Bundle args = new Bundle();
        args.putInt(ARG_TOP_RESOURCE,
                R.layout.jp_yamato_malta_gallery_fragment_simple_recyclerview);
        args.putInt(ARG_RESOURCE, resource);
        args.putInt(ARG_EMPTY_RESOURCE, android.R.drawable.alert_light_frame);
        args.putInt(ARG_MAX_TASK_COUNT, ImageAdapter.LoadTask.MAX_TASK_COUNT);
        args.putInt(ARG_LAYOUT, GalleryFragmentParams.GRID_LAYOUT);
        args.putInt(ARG_SPAN_COUNT, 2);
        instance.setArguments(args);
    }

    public static void setArguments(Fragment instance, int resource, int spanCount) {
        Bundle args = new Bundle();
        args.putInt(ARG_TOP_RESOURCE,
                R.layout.jp_yamato_malta_gallery_fragment_simple_recyclerview);
        args.putInt(ARG_RESOURCE, resource);
        args.putInt(ARG_EMPTY_RESOURCE, android.R.drawable.alert_light_frame);
        args.putInt(ARG_MAX_TASK_COUNT, ImageAdapter.LoadTask.MAX_TASK_COUNT);
        args.putInt(ARG_LAYOUT, GalleryFragmentParams.GRID_LAYOUT);
        args.putInt(ARG_SPAN_COUNT, spanCount);
        instance.setArguments(args);
    }

    public static void setArguments(Fragment instance, int resource, int spanCount,
            ArrayList<Uri> data) {
        Bundle args = new Bundle();
        args.putInt(ARG_TOP_RESOURCE,
                R.layout.jp_yamato_malta_gallery_fragment_simple_recyclerview);
        args.putInt(ARG_RESOURCE, resource);
        args.putInt(ARG_EMPTY_RESOURCE, android.R.drawable.alert_light_frame);
        args.putInt(ARG_MAX_TASK_COUNT, ImageAdapter.LoadTask.MAX_TASK_COUNT);
        args.putInt(ARG_LAYOUT, GalleryFragmentParams.GRID_LAYOUT);
        args.putInt(ARG_SPAN_COUNT, spanCount);
        args.putStringArrayList(ARG_DATA, toStringArrayList(data));
        instance.setArguments(args);
    }

    public void setAdapterData(final ArrayList<Uri> data) {
        mDeferredOperations.offer(new Runnable() {
            @Override
            public void run() {
                mAdapter.setAdapterData(data);
            }
        });
    }

    public ArrayList<Uri> getAdapterData() {
        if (!mDeferredOperations.isReleased()) {
            throw new IllegalStateException("get field is not ready");
        }
        return mAdapter.getAdapterData();
    }

    public Uri getAdapterDataItem(int position) {
        if (!mDeferredOperations.isReleased()) {
            throw new IllegalStateException("get field is not ready");
        }
        return mAdapter.getAdapterDataItem(position);
    }

    public void setTopResource(int topResource) {
        //
        mTopResource = topResource;
        mIsTopResourceFieldAvailable = true;

        if (mAdapter != null) {
            throw new IllegalStateException("cannot change top resource");
        }
    }

    public void setResource(int resource) {
        //
        mResource = resource;
        mIsResourceFieldAvailable = true;

        if (mAdapter == null) {
            return;
        }

        ArrayList<Uri> data = mAdapter.getAdapterData();
        Map<String, ImageAdapter.Formatter> formatter = mFormatterPickable.pickFormatter();

        int position = 0;
        View child = mRecyclerView.getChildAt(0);
        if (child != null) {
            position = mRecyclerView.getChildAdapterPosition(child);
        }

        mAdapter.destroy();

        mAdapter = new ImageAdapter(mContext, mResource, data, mOnItemClickListener);
        setAdapter();
        mRecyclerView.scrollToPosition(position);
    }

    public void setEmptyResource(int emptyResource) {
        //
        mEmptyResource = emptyResource;
        mIsEmptyResourceFieldAvailable = true;

        if (mAdapter != null) {
            mAdapter.setEmptyResource(mEmptyResource);
        }
    }

    public void setMaxTaskCount(int maxTaskCount) {
        //
        mMaxTaskCount = maxTaskCount;
        mIsMaxTaskCountAvailable = true;

        if (mAdapter != null) {
            mAdapter.setMaxTaskCount(maxTaskCount);
        }
    }

    public void setLayout(int layout, int spanCount) {
        //
        mLayout = layout;
        mSpanCount = spanCount;
        mIsLayoutFieldAvailable = true;
        mIsSpanCountFieldAvailable = true;

        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(
                    GalleryFragmentParams.resolveLayoutManager(mContext, mLayout, mSpanCount));
        }
    }

    public void onAttach(Context context, Fragment fragment) {
        mContext = context;

        // bitmap loader
        if (fragment instanceof ImageAdapter.LoadTask.BitmapLoader) {
            mBitmapLoader = (ImageAdapter.LoadTask.BitmapLoader) fragment;
        } else if (context instanceof ImageAdapter.LoadTask.BitmapLoader) {
            mBitmapLoader = (ImageAdapter.LoadTask.BitmapLoader) context;
        } else {
            throw new IllegalArgumentException("couldn't resolve BitmapLoader");
        }

        // formatter pickable
        if (fragment instanceof FormatterPickable) {
            mFormatterPickable = (FormatterPickable) fragment;
        } else if (context instanceof FormatterPickable) {
            mFormatterPickable = (FormatterPickable) context;
        }

        // OnItemClickListener
        if (fragment instanceof ImageAdapter.OnItemClickListener) {
            mOnItemClickListener = (ImageAdapter.OnItemClickListener) fragment;
        } else if (context instanceof ImageAdapter.OnItemClickListener) {
            mOnItemClickListener = (ImageAdapter.OnItemClickListener) context;
        }
    }

    public void onDetach() {
        mBitmapLoader = null;
        mFormatterPickable = null;
        mOnItemClickListener = null;
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState, @Nullable Bundle args) {
        ArrayList<String> data = null;
        if (savedInstanceState != null) {
            mTopResource = savedInstanceState.getInt(SAVE_TOP_RESOURCE);
            mResource = savedInstanceState.getInt(SAVE_RESOURCE);
            mEmptyResource = savedInstanceState.getInt(SAVE_EMPTY_RESOURCE);
            mMaxTaskCount = savedInstanceState.getInt(SAVE_MAX_TASK_COUNT);
            mLayout = savedInstanceState.getInt(SAVE_LAYOUT);
            mSpanCount = savedInstanceState.getInt(SAVE_SPAN_COUNT, 2);
            data = savedInstanceState.getStringArrayList(SAVE_DATA);
        } else {
            if (args != null) {
                if (!mIsTopResourceFieldAvailable) {
                    mTopResource = args.getInt(ARG_TOP_RESOURCE);
                }
                if (!mIsResourceFieldAvailable) {
                    mResource = args.getInt(ARG_RESOURCE);
                }
                if (!mIsEmptyResourceFieldAvailable) {
                    mEmptyResource = args.getInt(ARG_EMPTY_RESOURCE);
                }
                if (!mIsMaxTaskCountAvailable) {
                    mMaxTaskCount = args.getInt(ARG_MAX_TASK_COUNT);
                }
                if (!mIsLayoutFieldAvailable) {
                    mLayout = args.getInt(ARG_LAYOUT);
                }
                if (!mIsSpanCountFieldAvailable) {
                    mSpanCount = args.getInt(ARG_SPAN_COUNT, 2);
                }
                data = args.getStringArrayList(ARG_DATA);
            }
        }

        // create recycler view
        View topView = inflater.inflate(mTopResource, container, false);
        if (topView instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) topView;
        } else {
            View view = topView.findViewById(R.id.recycler_view);
            if (view instanceof RecyclerView) {
                mRecyclerView = (RecyclerView) view;
            } else {
                throw new IllegalArgumentException("recycler view not found in layout");
            }
        }
        mRecyclerView.setLayoutManager(
                GalleryFragmentParams.resolveLayoutManager(mContext, mLayout, mSpanCount));

        // set adapter
        if (data != null) {
            mAdapter = new ImageAdapter(mContext, mResource, toUriArrayList(data),
                    mOnItemClickListener);
        } else {
            mAdapter = new ImageAdapter(mContext, mResource, null, mOnItemClickListener);
        }

        setAdapter();
        mDeferredOperations.release();

        return topView;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_TOP_RESOURCE, mTopResource);
        outState.putInt(SAVE_RESOURCE, mResource);
        outState.putInt(SAVE_EMPTY_RESOURCE, mEmptyResource);
        outState.putInt(SAVE_MAX_TASK_COUNT, mMaxTaskCount);
        outState.putInt(SAVE_LAYOUT, mLayout);
        outState.putInt(SAVE_SPAN_COUNT, mSpanCount);
        outState.putStringArrayList(SAVE_DATA, toStringArrayList(mAdapter.getAdapterData()));
    }

    public void onDestroy() {
        // destroy
        mAdapter.destroy();
    }

    private void setAdapter() {
        mAdapter.setEmptyResource(mEmptyResource);
        mAdapter.setMaxTaskCount(mMaxTaskCount);
        mAdapter.setBitmapLoader(mBitmapLoader);
        //formatter
        if (mFormatterPickable != null) {
            Map<String, ImageAdapter.Formatter> formatter = mFormatterPickable.pickFormatter();
            if (formatter != null) {
                mAdapter.swapFormatter(formatter);
            }
        }

        mRecyclerView.setAdapter(mAdapter);
    }

    //
    //
    //

    private static ArrayList<String> toStringArrayList(ArrayList<Uri> uriList) {
        ArrayList<String> stringList = new ArrayList<>();
        if (uriList != null) {
            for (int i = 0; i < uriList.size(); i++) {
                stringList.add(uriList.get(i).toString());
            }
        }
        return stringList;
    }

    private static ArrayList<Uri> toUriArrayList(ArrayList<String> stringList) {
        ArrayList<Uri> uriList = new ArrayList<>();
        if (stringList != null) {
            for (int i = 0; i < stringList.size(); i++) {
                uriList.add(Uri.parse(stringList.get(i)));
            }
        }
        return uriList;
    }

}