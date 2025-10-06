package com.google.android.gms.common.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
/* loaded from: classes.dex */
public interface IGmsServiceBroker extends IInterface {

    /* compiled from: com.google.android.gms:play-services-basement@@18.1.0 */
    public static abstract class Stub extends Binder implements IGmsServiceBroker {
        public Stub() {
            attachInterface(this, "com.google.android.gms.common.internal.IGmsServiceBroker");
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public final boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            IGmsCallbacks zzaaVar;
            if (i > 16777215) {
                return super.onTransact(i, parcel, parcel2, i2);
            }
            parcel.enforceInterface("com.google.android.gms.common.internal.IGmsServiceBroker");
            IBinder strongBinder = parcel.readStrongBinder();
            if (strongBinder == null) {
                zzaaVar = null;
            } else {
                IInterface iInterfaceQueryLocalInterface = strongBinder.queryLocalInterface("com.google.android.gms.common.internal.IGmsCallbacks");
                zzaaVar = iInterfaceQueryLocalInterface instanceof IGmsCallbacks ? (IGmsCallbacks) iInterfaceQueryLocalInterface : new zzaa(strongBinder);
            }
            if (i == 46) {
                getService(zzaaVar, parcel.readInt() != 0 ? GetServiceRequest.CREATOR.createFromParcel(parcel) : null);
                Preconditions.checkNotNull(parcel2);
                parcel2.writeNoException();
                return true;
            }
            if (i == 47) {
                if (parcel.readInt() != 0) {
                    zzaj.CREATOR.createFromParcel(parcel);
                }
                throw new UnsupportedOperationException();
            }
            parcel.readInt();
            if (i != 4) {
                parcel.readString();
                switch (i) {
                    case 1:
                        parcel.readString();
                        parcel.createStringArray();
                        parcel.readString();
                        if (parcel.readInt() != 0) {
                            break;
                        }
                        break;
                    case 2:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 23:
                    case 25:
                    case 27:
                    case 37:
                    case 38:
                    case 41:
                    case 43:
                        if (parcel.readInt() != 0) {
                            break;
                        }
                        break;
                    case 9:
                        parcel.readString();
                        parcel.createStringArray();
                        parcel.readString();
                        parcel.readStrongBinder();
                        parcel.readString();
                        if (parcel.readInt() != 0) {
                            break;
                        }
                        break;
                    case 10:
                        parcel.readString();
                        parcel.createStringArray();
                        break;
                    case 19:
                        parcel.readStrongBinder();
                        if (parcel.readInt() != 0) {
                            break;
                        }
                        break;
                    case 20:
                    case 30:
                        parcel.createStringArray();
                        parcel.readString();
                        if (parcel.readInt() != 0) {
                            break;
                        }
                        break;
                    case 34:
                        parcel.readString();
                        break;
                }
            }
            throw new UnsupportedOperationException();
        }
    }

    void getService(IGmsCallbacks iGmsCallbacks, GetServiceRequest getServiceRequest) throws RemoteException;
}
