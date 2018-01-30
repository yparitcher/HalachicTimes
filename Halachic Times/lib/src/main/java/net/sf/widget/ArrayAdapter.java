/*
 * Copyright 2012, Moshe Waisberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.widget;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Array adapter ported from {@link android.widget.ArrayAdapter} to {@link RecyclerView}.
 *
 * @author Moshe Waisberg
 */
public class ArrayAdapter<T, VH extends ArrayAdapter.ArrayViewHolder> extends RecyclerView.Adapter<VH> implements Filterable {

    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see {@link #getFilter()} to make a synchronized copy of
     * the original array of data.
     */
    private final Object mLock = new Object();

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private final int mResource;

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    protected List<T> mObjects;

    /**
     * Indicates whether the contents of {@link #mObjects} came from static resources.
     */
    private boolean mObjectsFromResources;

    /**
     * If the inflated resource is not a TextView, {@code mFieldId} is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private int mFieldId = 0;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called whenever
     * {@link #mObjects} is modified.
     */
    private boolean mNotifyOnChange = true;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    protected ArrayList<T> mOriginalValues;
    private ArrayFilter mFilter;

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a TextView to use when
     *         instantiating views.
     */
    public ArrayAdapter(@LayoutRes int resource) {
        this(resource, 0);
    }

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a layout to use when
     *         instantiating views.
     * @param textViewResourceId
     *         The id of the TextView within the layout resource to be populated
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId) {
        this(resource, textViewResourceId, new ArrayList<T>());
    }

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a TextView to use when
     *         instantiating views.
     * @param objects
     *         The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @NonNull T[] objects) {
        this(resource, 0, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a layout to use when
     *         instantiating views.
     * @param textViewResourceId
     *         The id of the TextView within the layout resource to be populated
     * @param objects
     *         The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId, @NonNull T[] objects) {
        this(resource, textViewResourceId, Arrays.asList(objects));
    }

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a TextView to use when
     *         instantiating views.
     * @param objects
     *         The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @NonNull List<T> objects) {
        this(resource, 0, objects);
    }

    /**
     * Constructor
     *
     * @param resource
     *         The resource ID for a layout file containing a layout to use when
     *         instantiating views.
     * @param textViewResourceId
     *         The id of the TextView within the layout resource to be populated
     * @param objects
     *         The objects to represent in the ListView.
     */
    public ArrayAdapter(@LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<T> objects) {
        this(resource, textViewResourceId, objects, false);
    }

    private ArrayAdapter(@LayoutRes int resource,
                         @IdRes int textViewResourceId, @NonNull List<T> objects, boolean objsFromResources) {
        mResource = resource;
        mObjects = objects;
        mObjectsFromResources = objsFromResources;
        mFieldId = textViewResourceId;
        setHasStableIds(true);
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object
     *         The object to add at the end of the array.
     */
    public void add(@Nullable T object) {
        int position = 0;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                position = mOriginalValues.size();
                mOriginalValues.add(object);
            } else {
                position = mObjects.size();
                mObjects.add(object);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyItemInserted(position);
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection
     *         The Collection to add at the end of the array.
     * @throws UnsupportedOperationException
     *         if the <tt>addAll</tt> operation
     *         is not supported by this list
     * @throws ClassCastException
     *         if the class of an element of the specified
     *         collection prevents it from being added to this list
     * @throws NullPointerException
     *         if the specified collection contains one
     *         or more null elements and this list does not permit null
     *         elements, or if the specified collection is null
     * @throws IllegalArgumentException
     *         if some property of an element of the
     *         specified collection prevents it from being added to this list
     */
    public void addAll(@NonNull Collection<? extends T> collection) {
        int position = 0;
        int count = collection.size();
        synchronized (mLock) {
            if (mOriginalValues != null) {
                position = mOriginalValues.size();
                mOriginalValues.addAll(collection);
            } else {
                position = mObjects.size();
                mObjects.addAll(collection);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyItemRangeInserted(position, count);
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items
     *         The items to add at the end of the array.
     */
    public void addAll(T... items) {
        int position = 0;
        int count = items.length;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                position = mOriginalValues.size();
                Collections.addAll(mOriginalValues, items);
            } else {
                position = mObjects.size();
                Collections.addAll(mObjects, items);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyItemRangeInserted(position, count);
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object
     *         The object to insert into the array.
     * @param index
     *         The index at which the object must be inserted.
     */
    public void insert(@Nullable T object, int index) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyItemInserted(index);
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object
     *         The object to remove.
     */
    public void remove(@Nullable T object) {
        int position = 0;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                position = mOriginalValues.indexOf(object);
                mOriginalValues.remove(position);
            } else {
                position = mObjects.indexOf(object);
                mObjects.remove(position);
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyItemRemoved(position);
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
            mObjectsFromResources = false;
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator
     *         The comparator used to sort the objects contained
     *         in this adapter.
     */
    public void sort(@NonNull Comparator<? super T> comparator) {
        int count = 0;
        synchronized (mLock) {
            if (mOriginalValues != null) {
                count = mOriginalValues.size();
                Collections.sort(mOriginalValues, comparator);
            } else {
                count = mObjects.size();
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyItemRangeChanged(0, count);
    }

    /**
     * Control whether methods that change the list ({@link #add}, {@link #addAll(Collection)},
     * {@link #addAll(Object[])}, {@link #insert}, {@link #remove}, {@link #clear},
     * {@link #sort(Comparator)}) automatically call {@link #notifyDataSetChanged}.  If set to
     * false, caller must manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p>
     * The default is true, and calling notifyDataSetChanged() resets the flag to true.
     *
     * @param notifyOnChange
     *         if true, modifications to the list will
     *         automatically call {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    @Nullable
    public T getItem(int position) {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item
     *         The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(@Nullable T item) {
        return mObjects.indexOf(item);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mResource, parent, false);
        return createArrayViewHolder(view, mFieldId);
    }

    protected VH createArrayViewHolder(View view, int fieldId) {
        return (VH) new ArrayViewHolder(view, fieldId);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    @NonNull
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public static class ArrayViewHolder<T> extends RecyclerView.ViewHolder {

        protected final TextView textView;

        public ArrayViewHolder(View itemView, int fieldId) {
            super(itemView);

            try {
                textView = (TextView) ((fieldId == 0) ? itemView : itemView.findViewById(fieldId));
            } catch (ClassCastException e) {
                Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
                throw new IllegalStateException(
                        "ArrayAdapter requires the resource ID to be a TextView", e);
            }
        }

        public void bind(T item) {
            if (item instanceof CharSequence) {
                textView.setText((CharSequence) item);
            } else {
                textView.setText(item.toString());
            }
        }
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    protected class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mObjects);
                }
            }

            if (TextUtils.isEmpty(prefix)) {
                final ArrayList<T> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();

                final ArrayList<T> values;
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<T> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    final T value = values.get(i);
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<T>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
                mNotifyOnChange = true;
            }
        }
    }
}