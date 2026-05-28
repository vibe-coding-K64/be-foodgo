package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.AddressRequest;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.repository.AddressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> layTatCaDiaChi(String userId) {
        log.info("Bat dau lay danh sach dia chi - Nguoi dung: {}", userId);

        try {
            List<Address> addresses = addressRepository.layTatCaDiaChi(userId);
            log.info("Da lay {} dia chi cua nguoi dung {}", addresses.size(), userId);
            return addresses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay danh sach dia chi cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay danh sach dia chi.");
        } catch (ExecutionException e) {
            log.error("Loi khi lay danh sach dia chi cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay danh sach dia chi.");
        }
    }

    public Address layMotDiaChi(String userId, String addressId) {
        log.info("Bat dau lay dia chi - Nguoi dung: {}, Dia chi: {}", userId, addressId);

        try {
            Address address = addressRepository.layMotDiaChi(userId, addressId);
            if (address == null) {
                log.warn("Dia chi [{}] khong ton tai cho nguoi dung {}", addressId, userId);
                throw BusinessException.diaChiKhongTimThay(addressId);
            }

            log.info("Da lay dia chi [{}] cua nguoi dung {} thanh cong", addressId, userId);
            return address;

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin dia chi.");
        } catch (ExecutionException e) {
            log.error("Loi khi lay dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin dia chi.");
        }
    }

    public Address themDiaChi(AddressRequest request) {
        log.info("Bat dau xu ly them dia chi - Nguoi dung: {}, Nhan: {}, isDefault: {}",
                request.getUserId(), request.getName(), request.getIsDefault());

        try {
            if (Boolean.TRUE.equals(request.getIsDefault())) {
                log.info("Yeu cau dat dia chi moi lam mac dinh, quet bo dia chi mac dinh cu");
                addressRepository.xoaTatCaDiaChiMacDinh(request.getUserId(), "");
            }

            Address address = Address.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .receiverName(request.getReceiverName())
                    .receiverPhone(request.getReceiverPhone())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                    .build();

            String addressId = addressRepository.taoDiaChi(request.getUserId(), address);
            address.setId(addressId);

            log.info("Da tao dia chi [{}] cho nguoi dung {} thanh cong - nhan: {}",
                    addressId, request.getUserId(), request.getName());
            return address;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi them dia chi cho nguoi dung [{}]: {}", request.getUserId(), e.getMessage());
            throw BusinessException.loiHeThong("Khong the them dia chi.");
        } catch (ExecutionException e) {
            log.error("Loi khi them dia chi cho nguoi dung [{}]: {}", request.getUserId(), e.getMessage());
            throw BusinessException.loiHeThong("Khong the them dia chi.");
        }
    }

    public Address suaDiaChi(String userId, String addressId, AddressRequest request) {
        log.info("Bat dau xu ly sua dia chi - Nguoi dung: {}, Dia chi: {}, Nhan moi: {}",
                userId, addressId, request.getName());

        try {
            Address diaChiHienTai = addressRepository.layMotDiaChi(userId, addressId);
            if (diaChiHienTai == null) {
                log.warn("Dia chi [{}] khong ton tai khi sua", addressId);
                throw BusinessException.diaChiKhongTimThay(addressId);
            }

            boolean wasDefault = Boolean.TRUE.equals(diaChiHienTai.getIsDefault());
            boolean wantsDefault = Boolean.TRUE.equals(request.getIsDefault());

            if (!wasDefault && wantsDefault) {
                log.info("Yeu cau dat dia chi [{}] lam mac dinh, quet bo dia chi mac dinh cu", addressId);
                addressRepository.xoaTatCaDiaChiMacDinh(userId, addressId);
            }

            Address address = Address.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .receiverName(request.getReceiverName())
                    .receiverPhone(request.getReceiverPhone())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                    .build();

            addressRepository.capNhatDiaChi(userId, addressId, address);
            address.setId(addressId);

            log.info("Da cap nhat dia chi [{}] cua nguoi dung {} thanh cong", addressId, userId);
            return address;

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi sua dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the sua dia chi.");
        } catch (ExecutionException e) {
            log.error("Loi khi sua dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the sua dia chi.");
        }
    }

    public void datDiaChiMacDinh(String userId, String addressId) {
        log.info("Bat dau dat dia chi [{}] lam mac dinh - Nguoi dung: {}", addressId, userId);

        try {
            Address diaChi = addressRepository.layMotDiaChi(userId, addressId);
            if (diaChi == null) {
                log.warn("Dia chi [{}] khong ton tai khi dat mac dinh", addressId);
                throw BusinessException.diaChiKhongTimThay(addressId);
            }

            if (Boolean.TRUE.equals(diaChi.getIsDefault())) {
                log.info("Dia chi [{}] da la dia chi mac dinh, khong can thay doi", addressId);
                return;
            }

            addressRepository.xoaTatCaDiaChiMacDinh(userId, addressId);
            addressRepository.datDiaChiMacDinh(userId, addressId);

            log.info("Da dat dia chi [{}] lam dia chi mac dinh thanh cong cho nguoi dung {}",
                    addressId, userId);

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi dat dia chi mac dinh [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the dat dia chi mac dinh.");
        } catch (ExecutionException e) {
            log.error("Loi khi dat dia chi mac dinh [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the dat dia chi mac dinh.");
        }
    }

    public void xoaDiaChi(String userId, String addressId) {
        log.info("Bat dau xoa dia chi [{}] - Nguoi dung: {}", addressId, userId);

        try {
            Address diaChi = addressRepository.layMotDiaChi(userId, addressId);
            if (diaChi == null) {
                log.warn("Dia chi [{}] khong ton tai khi xoa, coi nhu xoa thanh cong (idempotent)", addressId);
                return;
            }

            addressRepository.xoaDiaChi(userId, addressId);
            log.info("Da xoa dia chi [{}] cua nguoi dung {} thanh cong", addressId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi xoa dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa dia chi.");
        } catch (ExecutionException e) {
            log.error("Loi khi xoa dia chi [{}]: {}", addressId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa dia chi.");
        }
    }
}
