package ru.x5.motpsender.dao.utilites.soap;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import ru.x5.motpsender.dao.utilites.DataSigner;
import ru.x5.motpsender.dao.utilites.soap.generated.*;

import javax.xml.bind.JAXBElement;
import java.util.Base64;

/**
 * Класс для получения подписи по ГОСТ 2012 через SOAP ервис PI
 */
@Log4j2
public class PiDataSignerService extends WebServiceGatewaySupport implements DataSigner {

    @Value("${sap.pi.token.uri}")
    private String sapTokenUri;

    @Value("${sap.pi.sign.uri}")
    private String sapSignUri;

    @Value("${sap.pi.ecp.type}")
    private String ecpType;

    @Value("${sap.pi.crypto.ip}")
    private String ipCryptoPro;

    @Value("${sap.pi.pernr}")
    private String defaultPersonalNumber;

    private String signerId;

    ObjectFactory objectFactory = new ObjectFactory();

    /**
     * @return Внутренняя сущность для КриптоПро DSS. Не имеет значения для бизнес логики. Наличие поля ERROR
     * свидетельствует об ошибке
     * Важный параметр PERNR - табельный номер подписанта. При расширении на несколько юр. лиц необходима параметризация
     */
    @SuppressWarnings("unchecked")
    private DtCryptoproToken getCryptoToken() {
        log.trace("Start getting CryptoPro DSS token");
        DtCryptoproTokenGet dtCryptoproTokenGet = objectFactory.createDtCryptoproTokenGet();
        dtCryptoproTokenGet.setECPTYPE(ecpType);
        dtCryptoproTokenGet.setIPCRYPTOPRO(ipCryptoPro);
        if ("".equals(signerId) || signerId == null) {
            dtCryptoproTokenGet.setPERNR(defaultPersonalNumber);
        } else dtCryptoproTokenGet.setPERNR(signerId);
        JAXBElement<DtCryptoproToken> res = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(sapTokenUri, objectFactory.createMtCryptoproTokenGet(dtCryptoproTokenGet));
        log.trace("End getting CryptoPro DSS token");
        return res.getValue();
    }

    /**
     * @param dtCryptoproToken Внутренняя сущность для КриптоПро DSS
     * @param data Данные для подписания
     * @return Подписанные данные
     */
    @SuppressWarnings("unchecked")
    private DtCryptoproSign getSign(DtCryptoproToken dtCryptoproToken, String data) {
        log.trace("Start getting CryptoPro DSS signed data");
        DtCryptoproSignGet dtCryptoproSignGet = objectFactory.createDtCryptoproSignGet();
        dtCryptoproSignGet.setBINARYSECRET(dtCryptoproToken.getBINARYSECRET());
        dtCryptoproSignGet.setECPTYPE(ecpType);
        dtCryptoproSignGet.setIPCRYPTOPRO(ipCryptoPro);
        dtCryptoproSignGet.setISDETACHED("X");
        if ("".equals(signerId) || signerId == null) {
            dtCryptoproSignGet.setPERNR(defaultPersonalNumber);
        } else dtCryptoproSignGet.setPERNR(signerId);
        dtCryptoproSignGet.setTOKEN(dtCryptoproToken.getTOKEN());
        dtCryptoproSignGet.setDOCUMENT(Base64.getEncoder().encodeToString(data.getBytes()));
        JAXBElement<DtCryptoproSign> res = (JAXBElement) getWebServiceTemplate()
                .marshalSendAndReceive(sapSignUri, objectFactory.createMtCryptoproSignGet(dtCryptoproSignGet));
        log.trace("End getting CryptoPro DSS signed data");
        return res.getValue();
    }

    /** Подписание строки с помощью вызова КриптоПро DSS через интерфейс SAP PI
     * @param data строка для подписания
     * @return подписанные данные
     * @throws PiSignerException
     */
    @Override
    public String signData(String data, String signerId) throws PiSignerException {
        this.signerId = signerId;
        DtCryptoproToken dtCryptoproToken = getCryptoToken();
        if (!"".equals(dtCryptoproToken.getERROR()) && dtCryptoproToken.getERROR() != null) {
            throw new PiSignerException("SAP PI return error during CryptoPro DSS get token process:" + dtCryptoproToken.getERROR());
        }
        DtCryptoproSign dtCryptoproSign = getSign(dtCryptoproToken, data);
        if (dtCryptoproSign.getDATA() == null)
            throw new PiSignerException("SAP PI return error during CryptoPro DSS get signed data process:" + dtCryptoproSign.getCOMMENT());
        return dtCryptoproSign.getDATA();
    }
}
