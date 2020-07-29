package brahma.vmi.covid2019.wcitui.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.LinkedList;

import brahma.vmi.covid2019.wcitui.tab.BaseFragment;


public class TabFragmentPagerAdapter extends FragmentPagerAdapter {

	LinkedList<BaseFragment> fragments = null;
	
	public TabFragmentPagerAdapter(FragmentManager fm, LinkedList<BaseFragment> fragments) {
		super(fm);
		if (fragments == null) {
			this.fragments = new LinkedList<BaseFragment>();
		}else{
			this.fragments = fragments;
		}
	}

	@Override
	public BaseFragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return fragments.get(position).getTitle();
	}
	
	public int getIconResId(int position) {
		return fragments.get(position).getIconResId();
	}

}
