package eu.romainpellerin.remotecontrolviasms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final List<String> values;
 
	public CustomArrayAdapter(Context context, List<String> values) {
		super(context, R.layout.item_menu, values);
		this.context = context;
		this.values = values;
	}
 
	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_menu, parent, false);
		}
		TextView textView = rowView.findViewById(R.id.label);
		ImageView imageView = rowView.findViewById(R.id.logo);
		textView.setText(values.get(position));
 
		switch (position) {
		case 0:
			imageView.setImageResource(R.drawable.home);
			break;
		case 1:
			imageView.setImageResource(R.drawable.wifi);
			break;
		case 2:
			imageView.setImageResource(R.drawable.data);
			break;
		case 3:
			imageView.setImageResource(R.drawable.alarm);
			break;
		case 4:
			imageView.setImageResource(R.drawable.gps);
			break;
		case 5:
			imageView.setImageResource(R.drawable.emergency);
			break;
		case 6:
			imageView.setImageResource(R.drawable.root);
			break;
		}
 
		return rowView;
	}
}
