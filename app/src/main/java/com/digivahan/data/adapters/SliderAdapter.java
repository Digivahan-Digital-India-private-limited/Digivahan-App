package com.digivahan.data.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.digivahan.R;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class SliderAdapter extends PagerAdapter {

    ArrayList<Slider> dataList;
    Activity activity;
    int layout;
    String from;

    public SliderAdapter(ArrayList<Slider> dataList, Activity activity, int layout, String from) {
        this.dataList = dataList;
        this.activity = activity;
        this.layout = layout;
        this.from = from;
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        View imageLayout = LayoutInflater.from(activity).inflate(layout, view, false);

        assert imageLayout != null;
        ImageView imgslider = imageLayout.findViewById(R.id.imgslider);
        LinearLayout lytmain = imageLayout.findViewById(R.id.lytmain);

        final Slider singleItem = dataList.get(position);

        /*Glide.with(activity).load(BaseUrl.BASE_URL + singleItem.getImage())
                .skipMemoryCache(true)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imgslider);*/

        view.addView(imageLayout, 0);

        /*lytmain.setOnClickListener(v -> {
            addBannerCount(activity, Constant.getString(activity, Session.VENDOR_ID), dataList.get(position).getId());
            Class nextClass = getSliderClass(dataList.get(position).getType());
            if (dataList.get(position).getType() != null && !dataList.get(position).getType().trim().isEmpty()
            && nextClass != null) {
                Intent invoiceClass = new Intent(activity, nextClass);
                activity.startActivity(invoiceClass);
            }else if (dataList.get(position).getContact() != null && dataList.get(position).getContact().length() == 10){
                call_action(dataList.get(position).getContact());
            }else if (dataList.get(position).getUrl() != null && !dataList.get(position).getUrl().trim().isEmpty()) {
                Intent intent = new Intent(activity, WebViewActivity.class);
                intent.putExtra("type", "Slider");
                intent.putExtra("url", dataList.get(position).getUrl().trim());
                activity.startActivity(intent);
            }
        });*/

        return imageLayout;
    }


    @Override
    public int getCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public void call_action(String phone_number) {
        /*// Permission already granted, proceed with the call
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phone_number));
            activity.startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(activity, "This service is not available, Please allow call permission", Toast.LENGTH_SHORT).show();
        }*/

        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phone_number));
        activity.startActivity(dialIntent);

    }

    /*public Class getSliderClass(String type){
        Class aClass = null;
        if (type.equalsIgnoreCase("1")){
            aClass = InvoiceActivity.class;
        }else if (type.equalsIgnoreCase("2")){
            aClass = PostNewRide.class;
        }else if (type.equalsIgnoreCase("3")){
            aClass = Membership.class;
        }else if (type.equalsIgnoreCase("4")){
            aClass = CityActivity.class;
        }else if (type.equalsIgnoreCase("5")){
            aClass = DonationPage.class;
        }else if (type.equalsIgnoreCase("6")){
            aClass = LoanFormPage.class;
        }else if (type.equalsIgnoreCase("7")){
            aClass = InsuranceFormPage.class;
        }else if (type.equalsIgnoreCase("8")){
            aClass = GSTApplyPage.class;
        } else if (type.equalsIgnoreCase("9")){
//            itonary
//            aClass = GSTApplyPage.class;
        }else if (type.equalsIgnoreCase("10")) {
            aClass = MSMEApplyPage.class;
        }
        return aClass;
    }*/

}
