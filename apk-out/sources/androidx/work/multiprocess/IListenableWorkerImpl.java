package androidx.work.multiprocess;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import androidx.work.multiprocess.IWorkManagerImplCallback;
import kotlin.text.Typography;

/* loaded from: classes.dex */
public interface IListenableWorkerImpl extends IInterface {
    public static final String DESCRIPTOR = "androidx$work$multiprocess$IListenableWorkerImpl".replace(Typography.dollar, '.');

    void interrupt(byte[] request, IWorkManagerImplCallback callback) throws RemoteException;

    void startWork(byte[] request, IWorkManagerImplCallback callback) throws RemoteException;

    public static class Default implements IListenableWorkerImpl {
        @Override // androidx.work.multiprocess.IListenableWorkerImpl
        public void startWork(byte[] request, IWorkManagerImplCallback callback) throws RemoteException {
        }

        @Override // androidx.work.multiprocess.IListenableWorkerImpl
        public void interrupt(byte[] request, IWorkManagerImplCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IListenableWorkerImpl {
        static final int TRANSACTION_interrupt = 2;
        static final int TRANSACTION_startWork = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IListenableWorkerImpl asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && (iin instanceof IListenableWorkerImpl)) {
                return (IListenableWorkerImpl) iin;
            }
            return new Proxy(obj);
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String descriptor = DESCRIPTOR;
            if (code >= 1 && code <= 16777215) {
                data.enforceInterface(descriptor);
            }
            if (code == 1598968902) {
                reply.writeString(descriptor);
                return true;
            }
            switch (code) {
                case 1:
                    byte[] _arg0 = data.createByteArray();
                    IWorkManagerImplCallback _arg1 = IWorkManagerImplCallback.Stub.asInterface(data.readStrongBinder());
                    startWork(_arg0, _arg1);
                    return true;
                case 2:
                    byte[] _arg02 = data.createByteArray();
                    IWorkManagerImplCallback _arg12 = IWorkManagerImplCallback.Stub.asInterface(data.readStrongBinder());
                    interrupt(_arg02, _arg12);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IListenableWorkerImpl {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            @Override // androidx.work.multiprocess.IListenableWorkerImpl
            public void startWork(byte[] request, IWorkManagerImplCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(request);
                    _data.writeStrongInterface(callback);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // androidx.work.multiprocess.IListenableWorkerImpl
            public void interrupt(byte[] request, IWorkManagerImplCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeByteArray(request);
                    _data.writeStrongInterface(callback);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
