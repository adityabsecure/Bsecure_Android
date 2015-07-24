
package com.mail.bsecure.controls;

//**************************************************************************/

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bsecure.mail.R;

//**************************************************************************/

public class ButtonsToggle extends LinearLayout
{
	//**************************************************************************/
	
	private boolean isChecked = true;

	private OnCheckedChangeListener m_listener;
	
	private View yesView = null;
	private View noView = null;

	//**************************************************************************/

	public interface OnCheckedChangeListener
	{
		public void onCheckedChanged(ButtonsToggle button, boolean isChecked);

	}

	//**************************************************************************/

	private void init()
	{

        setOrientation(LinearLayout.HORIZONTAL);

		setPadding(2, 2, 2, 2);
		setLayoutParams(new LayoutParams(150,34));

        yesView = View.inflate(this.getContext(), R.layout.toggle_buttons, null);
        yesView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        ((TextView)yesView).setText("Yes");

       noView = View.inflate(this.getContext(), R.layout.toggle_buttons, null);
        noView.setBackgroundColor(getResources().getColor(R.color.toggle_no));
        ((TextView)noView).setText("No");


        this.addView(yesView);
        this.addView(noView);

        yesView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                yesView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                noView.setBackgroundColor(getResources().getColor(R.color.toggle_no));
                m_listener.onCheckedChanged(ButtonsToggle.this, true);
                isChecked = true;
            }
        });

        noView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                noView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                yesView.setBackgroundColor(getResources().getColor(R.color.toggle_no));
                m_listener.onCheckedChanged(ButtonsToggle.this, false);
                isChecked = false;
            }
        });

		invalidate();
	}
	
	public void setChecked() {
		isChecked = true;
	}
	
	public void setUnChecked() {
		noView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        yesView.setBackgroundColor(getResources().getColor(R.color.toggle_no));
		isChecked = false;
	}

	//**************************************************************************/

	public ButtonsToggle(Context context)
	{
		super(context);

		init();
	}

	//**************************************************************************/

	public ButtonsToggle(Context context, AttributeSet attrs)
	{
		super(context,attrs);
		
		init();
	}
	
	//**************************************************************************/
	
	public void setOnCheckedChangeListener( OnCheckedChangeListener listener )
	{
		m_listener = listener;
	}
	
	//**************************************************************************/
	
	public boolean isChecked()
	{
		return isChecked;
	}
	
}

//**************************************************************************/
