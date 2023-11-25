package com.minosapps.aiwp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.android.material.tabs.TabLayoutMediator;
import com.minosapps.aiwp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    public Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Initialize and schedule the wallpaper change task
        BackgroundTaskManager taskManager = new BackgroundTaskManager(this);
        taskManager.scheduleWallpaperChange();

        settings = new Settings(this);


        // Setup the ViewPager with an adapter and link it with the TabLayout
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position){
                        case 0:
                            tab.setText("Setup");
                            tab.setIcon(R.drawable.outline_science_24);
                            return;
                        case 1:
                            tab.setText("Location");
                            tab.setIcon(R.drawable.outline_location_on_24);
                            return;
                        case 2:
                            tab.setText("Content");
                            tab.setIcon(R.drawable.outline_text_fields_24);
                            return;
                        case 3:
                            tab.setText("Preview");
                            tab.setIcon(R.drawable.outline_image_24);
                            return;
                        default:
                            return;
                    }
                }).attach();

        UITools.setupSwitch(binding.activeSwitch, isChecked -> {
            settings.setActive(isChecked);
            updateActiveStatus();
        });
        updateActiveStatus();

        settings.addCallback(new AIWPCallback() {
            @Override
            public void onCallback() {updateActiveStatus();}
        });
        settings.rescheduleWallpaperChange();
    }


    class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0:
                    return new ApiFragment();
                case 1:
                    return new ContextFragment();
                case 2:
                    return new ContentFragment();
                case 3:
                    return new PreviewFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private void updateActiveStatus(){
        if(!ApiFragment.isApiKeyValid(settings)){
            binding.activeStatus.setText("No valid API Key. Check the Setup tab!");
            binding.activeSwitch.setChecked(false);
            binding.activeSwitch.setEnabled(false);
            return;
        }
        boolean isActive = settings.getActive();
        binding.activeStatus.setText(isActive ? "Active" : "Inactive");
        binding.activeSwitch.setEnabled(true);
        binding.activeSwitch.setChecked(isActive);
    }

}
